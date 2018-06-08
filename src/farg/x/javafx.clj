(ns farg.x.javafx
  "Experiments: Seeing if I can get JavaFX to work from Clojure."
  (:refer-clojure :exclude [cond])
  (:require [better-cond.core :refer [cond]]
            [clojure.tools.trace :refer [deftrace] :as trace]
            [clojure.pprint :refer [pprint]])
  (:import [javafx.event ActionEvent EventHandler]
           [javafx.scene Scene SceneBuilder]
           [javafx.scene.control Button ButtonBuilder]
           [javafx.scene.layout VBoxBuilder StackPane]
           [javafx.stage Stage StageBuilder]))

;;; Code largely taken from https://gist.github.com/zilti/6286307

(defn run-later*
  [f]
  (javafx.application.Platform/runLater f))

(defmacro run-later
  [& body]
  `(run-later* (fn [] ~@body)))

(defn run-now*
  [f]
  (let [result (promise)]
    (run-later
     (deliver result (try (f) (catch Throwable e e))))
    @result))

(defmacro run-now
  [& body]
  `(run-now* (fn [] ~@body)))

(defn event-handler*
  [f]
  (reify javafx.event.EventHandler
    (handle [this e] (f e))))

(defmacro event-handler [arg & body]
  `(event-handler* (fn ~arg ~@body)))



(defn hw []
  (proxy [javafx.application.Application] []
    (start [this primary-stage]
      (.setTitle primary-stage "Hello, World!")
      (let [btn (doto (new Button)
                  (.setText "Hello, World!")
                  (.setOnAction (event-handler [_] (println "Hello, World!"))))
            root (new StackPane)]
        (.add (.getChildren root) btn)
        (.setScene primary-stage (new Scene root 300 250))
        (.show primary-stage)))
    (main [this args]
      (.launch this args))))

(defn mkstage []
  (.. StageBuilder create
      (title "Hello, JavaFX")
      (scene (.. SceneBuilder create
                 (height 480) (width 640)
                 (root (.. VBoxBuilder create
                           (minHeight 480) (minWidth 640)
                           (children [(.. ButtonBuilder create
                                          (text "Say \"Hello, Clojure\"!")
                                          (onAction (event-handler [_]
                                                      (println "Hello, Clojure!")))
                                          build)])
                           build))
                 build))
      build))

(defn mkstage2 []
  (let [btn (doto (new Button)
              (.setText "Hello, World!")
              (.setOnAction (event-handler [_] (println "Hello, World!"))))
        root (new StackPane)
        primary-stage (new Stage)]
    (.add (.getChildren root) btn)
    (.setScene primary-stage (new Scene root 300 250))
    primary-stage))

(def stage (atom nil))

(defn new-stage! [old-st new-st]
  (when (= javafx.stage.Stage (class old-st))
    (.close old-st))
  (doto new-st .show .toFront))

(defn go []
  (run-now (swap! stage new-stage! (mkstage2))))
