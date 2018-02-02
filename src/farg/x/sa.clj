(ns farg.x.sa
  "Spreading activation"
  (:refer-clojure :exclude [cond rand rand-int])
  (:require [better-cond.core :refer [cond]]
            [clojure.pprint :refer [pprint]]
            [com.rpl.specter :as S :refer :all]
            [farg.util :as util :refer
              [dd dde with-rng-seed rand rand-int choose-one]]
            [farg.with-state :refer [with-state]]
            [farg.x.navs :refer :all]
            [ubergraph.core :as uber :refer [ubergraph?]]))

(def ^{:doc "Activation"} A (attr :a))

(def decay 0.1)

(defn sat+
  "Saturating addition: sum is limited to interval [-1.0, +1.0]."
  [& args]
  (cond
    :let [raw-sum (apply + args)]
    (< raw-sum -1.0) -1.0
    (> raw-sum +1.0) +1.0
    raw-sum))

(defn incoming-to [gnode]
  (->> gnode
    (traverse [IN-EDGES (collect-one WEIGHT) SRC A])
    (reduce (fn [total [weight a]] (+ total (* weight a))) 0)))

(defn spread [g]
  (transform [NODES VAL A]
             (fn [gnode a]
               (sat+ a (* decay (incoming-to gnode))))
             g))

;(def INCOMING-ACTIVATIONS
;  (traversed (transformed [IN-EDGES (collect-one WEIGHT) SRC A] *)
;             +))
;
;(defn s2 [g]
;  (transform [NODES (collect-one INCOMING-ACTIVATIONS) A]
;             (fn [incomings a]
;               (let [incoming
;               (sat+ (* decay incoming) a))))

;;; Code to call 'spread'

(defn random-graph [{:keys [num-nodes num-edges]
                     :or {num-nodes 6, num-edges 10}}]
  (with-state [g (uber/digraph)]
    (doseq [n (range num-nodes)]
      (bind a (rand -1.0 +1.0))
      (uber/add-nodes-with-attrs [n {:a a :start-a a}]))
    (dotimes [_ num-edges]
      (bind n1 (rand-int num-nodes))
      (bind n2 (rand-int num-nodes))
      (bind weight (choose-one -1.0 +1.0))
      (uber/add-edges [n1 n2 weight]))))

(defn print-activations [g]
  (apply println (select [NODES A] g)))

(defn run [& {:keys [num-timesteps :as opts]
              :or {num-timesteps 20}}]
  (with-state [g (random-graph opts)]
    -- (print-activations g)
    (dotimes [t num-timesteps]
      (spread)
      -- (print-activations g))))
