# test.rpn
-- name should probably be changed

A Clojure "library" for solving infix expressions.

## Usage

There are a number of functions to do each part of the parsing and calculation.  
The test.rpn/regexes map contains regexes for each of the different tokens recognized by the lexical parser.  
The test.rpn/shunt function implements a [shunting yard algorithm](https://en.wikipedia.org/wiki/Shunting-yard_algorithm) modified to work recursively instead of iteratively.  
The test.rpn/evalExpr function evaluates a postfix expression using the regexes and functions provided in the test.rpn/regexes and test.rpn/funcs maps.  

The function test.rpn/solve takes in an infix expression string
and evaluates it and returns the result.

## License

Copyright © 2016 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
