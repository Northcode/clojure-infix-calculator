(ns test.core
  (:gen-class))
(require 'clojure.string)
(require 'test.rpn)

(defn -main
  "I don't do a whole lot yet"
  [& args]
  (test.rpn/input-loop))

