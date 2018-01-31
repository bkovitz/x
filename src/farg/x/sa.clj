(ns farg.x.sa
  "Spreading activation"
  (:refer-clojure :exclude [cond])
  (:require [better-cond.core :refer [cond]]
            [clojure.pprint :refer [pprint]]
            [com.rpl.specter :as S :refer :all]
            [farg.with-state :refer [with-state]]
            [farg.x.navs :refer :all]
            [ubergraph.core :as uber :refer [ubergraph?]]))

(def ^{:doc "Activation"} A (attr :a))

(def decay 1.0)
(def sat+ +)

(defn incoming-activation [g node]
  (->> g
    (select [(find-node node) IN-EDGES (collect ATTRS :weight) SRC ATTRS :a])
    (map (fn [[[weight] a]]
           (* weight a)))
    (reduce +)
    (* decay)))

(defn spread [g]
  (transform [NODES (collect) ATTRS :a]
             (fn [[node] a]
               (sat+ a (incoming-activation g node)))
             g))
