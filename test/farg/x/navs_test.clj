(ns farg.x.navs-test
  (:refer-clojure :exclude [cond])
  (:require [better-cond.core :refer [cond]]
            [clojure.pprint :refer [pprint]]
            [clojure.test :refer :all]
            [com.rpl.specter :as S :refer :all]
            [farg.with-state :refer [with-state]]
            [farg.x.navs :refer :all]
            [ubergraph.core :as uber :refer [ubergraph?]]))

(deftest test-find-node
  (let [g (uber/digraph [:a {:weight 1}] [:a :b 25] [:b :c 80] [:c :d]
                        [:d :a -10])
        g' (uber/remove-nodes g :b)]
    (is (= [:b] (select [(find-node :b) ELEM] g)))
    (is (= g' (setval [(find-node :b)] NONE g)))
    ))
    ;TODO transform ELEM

(deftest test-node-attr
  (let [g     (uber/digraph [:a {:weight 1}] [:b {:weight 2}])
        g-inc (uber/digraph [:a {:weight 1}] [:b {:weight 3}])
        g-rm  (uber/digraph [:a {:weight 1}] [:b {}])
        g-10  (uber/digraph [:a {:weight 1}] [:b {:weight 10}])]
    (is (= [2]   (select    [(find-node :b) (attr :weight)] g)))
    (is (= g-inc (transform [(find-node :b) (attr :weight)] inc g)))
    (is (= g-rm  (setval    [(find-node :b) (attr :weight)] NONE g)))
    (is (= g-10  (setval    [(find-node :b) (attr :weight)] 10 g)))))

(deftest test-node-attrs
  (let [g     (uber/digraph [:a {:weight 1}] [:b {:weight 2}])
        g-inc (uber/digraph [:a {:weight 1}] [:b {:weight 3}])
        g-rm  (uber/digraph [:a {:weight 1}] [:b {}])
        g-10  (uber/digraph [:a {:weight 1}] [:b {:weight 10}])]
    (is (= [2]   (select    [(find-node :b) ATTRS :weight] g)))
    (is (= g-inc (transform [(find-node :b) ATTRS :weight] inc g)))
    (is (= g-rm  (setval    [(find-node :b) ATTRS :weight] NONE g)))
    (is (= g-10  (setval    [(find-node :b) ATTRS :weight] 10 g)))))

(deftest test-node-weight
  (let [g     (uber/digraph [:a {:weight 1}] [:b {:weight 2}])
        g-inc (uber/digraph [:a {:weight 1}] [:b {:weight 3}])
        g-rm  (uber/digraph [:a {:weight 1}] [:b {}])
        g-10  (uber/digraph [:a {:weight 1}] [:b {:weight 10}])]
    (is (= [2]   (select    [(find-node :b) WEIGHT] g)))
    (is (= g-inc (transform [(find-node :b) WEIGHT] inc g)))
    (is (= g-rm  (setval    [(find-node :b) WEIGHT] NONE g)))
    (is (= g-10  (setval    [(find-node :b) WEIGHT] 10 g)))))

(deftest test-nodes
  (let [g (uber/digraph [:a {:weight 1}] [:a :b 25] [:b :c 80] [:c :d]
                        [:d :a -10])]
    (is (= #{:a :b :c :d} (set (select [NODES ELEM] g))))
    ))

(deftest test-edge-weight
  (let [g     (uber/digraph [:a :b 25] [:b :c 50])]
    (is (= [25] (select [(find-node :b) IN-EDGES WEIGHT] g)))
    ))

(def g (uber/digraph [:a {:weight 1}] [:a :b 25] [:b :c 80] [:c :d]
                     [:d :a -10]))

;(pprint (select [NODES ATTRS] g))

#_(pprint (select [(find-node :b) IN-EDGES ELEM] g))

#_(pprint (select [(find-node :b) IN-EDGES SRC ELEM] g))

#_(pprint (select [(find-node :b) IN-EDGES WEIGHT] g))
