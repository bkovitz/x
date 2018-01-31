(ns farg.x.navs
  (:refer-clojure :exclude [cond])
  (:require [better-cond.core :refer [cond]]
            [clojure.tools.trace :refer [deftrace] :as trace]
            [clojure.pprint :refer [pprint]]
            [com.rpl.specter :as S :refer :all]
            [com.rpl.specter.util-macros :refer [doseqres]]
            [farg.with-state :refer [with-state]]
            #_[loom.graph :as lg]
            #_[loom.attr :as la]
            [ubergraph.core :as uber :refer [ubergraph?]]))

(defn- rename-nodes [g m-old->new]
  ;STUB TODO
  (println "rename-nodes is unimplemented.")
  g)

(defnav
  ^{:doc "Find node in graph g."}
  find-node [node]
  (select* [this g next-fn]
    (if (uber/has-node? g node)
      (next-fn [g node])
      nil))
  (transform* [this g next-fn]
    (cond
      :let [ret (next-fn [g node])]
      (identical? ret NONE)
        (uber/remove-nodes g node)
      :let [[g newnode] ret]
      (identical? newnode NONE)
        (uber/remove-nodes g node)
      (= node newnode)
        g
      (rename-nodes g {node newnode}))))

(defnav
  ^{:doc "Strips [g node-edge-or-port] down to node-edge-or-port."}
  ELEM []
  (select* [this [g elem] next-fn]
    (next-fn elem))
  (transform* [this [g elem] next-fn]
    [g (next-fn elem)]))

(defn has-elem? [g elem]
  (or (uber/has-node? g elem)
      (and (uber/edge? elem)
           (uber/has-edge? g (uber/src elem) (uber/dest elem)))))

(defnav
  ^{:doc "Attribute of node or edge."}
  attr [k]
  (select* [this [g elem] next-fn]
    (if (has-elem? g elem)
      (next-fn (uber/attr g elem k))
      nil))
  (transform* [this [g elem] next-fn]
    (let [newv (next-fn (uber/attr g elem k))
          newg (if (identical? newv NONE)
                 (if (has-elem? g elem)
                   (uber/remove-attr g elem k)
                   g)
                 (uber/add-attr g elem k newv))]
      [newg elem])))

(def WEIGHT (attr :weight))

(defnav
  ^{:doc "Attribute map of node or edge."} ;TODO support edge attrs
  ATTRS []
  (select* [this [g node] next-fn]
    (next-fn (uber/attrs g node)))
  (transform* [this [g node] next-fn]
    (let [ret (next-fn (uber/attrs g node))
          newg (uber/set-attrs g node ret)]
      [newg node])))

;(defn update-nodes [g {:keys [renamed removed new-attrs]}]

(defnav
  ^{:doc "All nodes in graph g."} ;TODO 
  NODES []
  (select* [this g next-fn]
    (doseqres NONE [node (uber/nodes g)]
      (next-fn [g node])))
  (transform* [this g next-fn]
    #_(with-state [m {:renamed {} :removed #{} :new-attrs {}}]
      (doseq [oldnode (uber/nodes g)]
        ;I think this is wrong. How do we find out about new nodes?
        (bind ret (next-fn [g oldnode]))
        (if (identical? ret NONE)
          (update :removed conj oldnode)
          (do
            (bind [newg newnode] ret)
            (if (identical? newnode NONE)
              (update :removed conj oldnode)
              (do
                (bind new-attrs (uber/attrs newg newnode))
                (when (not (identical? new-attrs (uber/attrs g oldnode)))
                  (update :new-attrs assoc newnode new-attrs))
                (when (not= oldnode newnode)
                  (update :renamed assoc oldnode newnode)))))))
      (update-nodes g m))
    g ;TODO STUB  I'm not sure of next-fn's calling protocol here
    ))

;TODO UT
(defnav
  ^{:doc "All incoming edges of current node."}
  IN-EDGES []
  (select* [this [g node] next-fn]
    (doseqres NONE [edge (uber/in-edges g node)]
      (next-fn [g edge])))
  (transform* [this [g node] next-fn]
    ;STUB
    (doseq [edge (uber/in-edges g node)]
      (next-fn [g edge]))
    [g node]))

;TODO UT
(defnav
  ^{:doc "Src node of current edge."}
  SRC []
  (select* [this [g edge] next-fn]
    (next-fn [g (uber/src edge)]))
  (transform* [this [g edge] next-fn]
    ;STUB
    (next-fn [g (uber/src edge)])
    [g edge]))
