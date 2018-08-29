#lang racket

(require graph)

(define g (unweighted-graph/directed '((a b) (b c) (c d) (d a))))
