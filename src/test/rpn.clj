(ns test.rpn
  (:require [clojure.string :as s]))

(def funcs
  "Map of functions to add to the evaluator,
  keys of the map are the function names and 
  values are vectors where the first entry is the arity 
  and the second is the function to call"
  {
   "sin" [1 #(Math/sin %)]
   "cos" [1 #(Math/cos %)]
   "max" [2 #(max % %2)]
   "acos" [1 #(Math/acos %)]
   })

(def oprs {"+" + "-" - "*" * "/" / "%" mod})

(def regexes
  "Contains regexes used by the matcher to separate the input string into a sequence of tokens"
  {:num #"\d+"
   :wrd #"\p{L}+"
   :opr (re-pattern (str "[" (s/join "|" (map #(if (= % "-") "\\-" %) (keys oprs))) "]"))
   :par #"[\(\)]"
   :func (re-pattern (s/join "|" (keys funcs)))})

(regexes :opr)

(def mre
  "Combined regex for matching any token"
  (re-pattern (s/join "|" (vals regexes))))

(defn is-op? [v]
  "Check if <v> is an operator token"
  (re-matches (regexes :opr) v))

(def precedences
  "Map of precedences for operators"
  (merge
   {"+" 1 "-" 1 "*" 2 "/" 2 "%" 3 "(" 0}
   (reduce-kv (fn [m k v] (assoc m k 4)) {} funcs)))

(defn shunt
  "Convert a list of tokens <inp> from infix to postfix order.
  Uses <opr> as operator stack, and <out> as a qeueue for building the result"
  ([inp] (shunt inp [] []))
  ([inp opr] (shunt inp opr []))
  ([inp opr out]

  ;; (println inp opr out)

  (let [inp-empty? (empty? inp)
        opr-empty? (empty? opr)
        tok (first inp)
        open-paren (= tok "(")
        close-paren (= tok ")")
        just-opened (= (peek opr) "(")
        tok-num? (and tok (re-matches (regexes :num) tok))
        tok-op? (and tok (is-op? tok))
        opr-not-empty ((comp not empty?) opr)
        precedence-less? (delay (< (precedences tok) (precedences (peek opr))))
        tok-func? (and tok (re-matches (regexes :func) tok))
        opr-func? (and (not opr-empty?) (re-matches (regexes :func) (peek opr)))
        ]
    (cond
      ;; Done, return result
      (and inp-empty? opr-empty?)
      out

      ;; Finish up
      inp-empty?
      (recur inp (pop opr) (conj out (peek opr)))

      ;; Parenthesis
      open-paren
      (recur (rest inp) (conj opr tok) out)

      (and close-paren (not just-opened))
      (recur inp (pop opr) (conj out (peek opr)))

      (and close-paren just-opened)
      (recur (rest inp) (pop opr) out)

      (and close-paren opr-func? opr-func?)
      (recur inp (pop opr) (conj out (peek opr)))

      ;; Operators
      (and tok-op? opr-not-empty @precedence-less?)
      (recur inp (pop opr) (conj out (peek opr)))

      (and tok-op? opr-not-empty (not @precedence-less?))
      (recur (rest inp) (conj opr tok) out)

      tok-op?
      (recur (rest inp) (conj opr tok) out)

      ;; Functions
      tok-func?
      (recur (rest inp) (conj opr tok) out)

      ;; Numbers/Values
      tok-num?
      (recur (rest inp) opr (conj out (Integer. tok)))
      
      :else
      (recur (rest inp) opr (conj out tok))
      ))))
      
(defn evalExpr
  "Evaluate a postfix expression <expr> using <acc> as the accumulator stack"
  ([expr] (evalExpr expr []))
  ([expr acc]
   (let [
         tok (first expr)
         tok-str? (and tok (string? tok))
         tok-op? (and tok-str? (delay (is-op? tok)))
         tok-func? (and tok-str? (re-matches (regexes :func) tok))
         opr (first acc)
         ]
     ;; (println expr acc tok opr)
     (cond
       (empty? expr)
       (first acc)
       tok-func?
       (let [f (get-in funcs [tok 1])
             fa (get-in funcs [tok 0])
             args (reverse (take-last fa acc))
             ]
         (recur (rest expr) (conj (drop fa acc) (apply f args))))
       tok-op?
       (recur (rest expr) (conj (drop-last 2 acc) (apply (oprs tok) (reverse (take-last 2 acc)))))
       :else
       (recur (rest expr) (conj acc tok))
       ))))

(defn solve [input]
  (let [toks (re-seq mre input)
        rpn (shunt toks)
        result (evalExpr rpn)]
    result))

(defn input-loop []
  (loop []
    (print "Enter query: ")
    (flush)
    (let [input (read-line)]
      (if input
        (let [result (solve input)]
          (println "Result: " result)
          (recur))))))
