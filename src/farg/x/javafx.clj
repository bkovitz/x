(ns farg.x.javafx
  "Experiments: Seeing if I can get JavaFX to work from Clojure."
  (:refer-clojure :exclude [cond])
  (:require [better-cond.core :refer [cond]]
            [clojure.tools.trace :refer [deftrace] :as trace]
            [clojure.pprint :refer [pprint]])
  (:import [javafx.event ActionEvent EventHandler]
           [javafx.scene Scene SceneBuilder Group Node]
           [javafx.scene.canvas Canvas GraphicsContext]
           [javafx.scene.control Button ButtonBuilder]
           [javafx.scene.layout Pane VBoxBuilder StackPane]
           [javafx.scene.paint Color]
           [javafx.scene.shape Circle Line]
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
        root (doto (new StackPane)
               (.. getChildren (add btn)))
        primary-stage (doto (new Stage)
                        (.setScene (new Scene root 300 250)))]
    primary-stage))

(def stage (atom nil))

(defn new-stage! [old-st new-st]
  (when (= javafx.stage.Stage (class old-st))
    (.close old-st))
  (doto new-st .show .toFront))

(defn go
 ([]
  (go (mkstage2)))
 ([st]
  (run-now (swap! stage new-stage! st))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn circle? [x]
  (isa? x Circle))

(defn make-draggable [node]
  (let [originals (atom {:scene-x nil, :scene-y nil, :translate-x nil,
                         :translate-y nil})]
    (doto node
      (.setOnMousePressed
        (event-handler [e]
          (let [src (.getSource e)]
            (swap! originals assoc
              :scene-x (.getSceneX e)
              :scene-y (.getSceneY e)
              :translate-x (if (circle? src)
                             (.getCenterX src)
                             (.getTranslateX src))
              :translate-y (if (circle? src)
                             (.getCenterY src)
                             (.getTranslateY src))))))
      (.setOnMouseDragged
        (event-handler [e]
          (let [src (.getSource e)
                offsetX (- (.getSceneX e) (:scene-x @originals))
                offsetY (- (.getSceneY e) (:scene-y @originals))
                newX (+ (:translate-x @originals) offsetX)
                newY (+ (:translate-y @originals) offsetY)]
            (if (circle? src)
              (doto src
                (.setCenterX newX)
                (.setCenterY newY))
              (doto src
                (.setTranslateX newX)
                (.setTranslateY newY)))))))))

(defn mkstage-dragnodes
  "Makes the Stage for the DragNodes app from
  https://stackoverflow.com/a/28000358/1393162"
  []
  (let [canvas (new Canvas 300 300)
        gc (doto (.getGraphicsContext2D canvas)
             (.setStroke Color/RED)
             (.strokeRoundRect 10 10 230 230 10 10))
        circle1 (doto (new Circle 50)
                  (.setStroke Color/GREEN)
                  (.setFill (.deriveColor Color/GREEN 1 1 1 0.7))
                  (.relocate 100 100)
                  make-draggable)
        circle2 (doto (new Circle 50)
                  (.setStroke Color/BLUE)
                  (.setFill (.deriveColor Color/BLUE 1 1 1 0.7))
                  (.relocate 200 200)
                  make-draggable)
        line (doto (new Line (.getLayoutX circle1)
                             (.getLayoutY circle1)
                             (.getLayoutX circle2)
                             (.getLayoutY circle2))
               (.setStrokeWidth 20)
               make-draggable)
        overlay (doto (new Pane) 
                  (.. getChildren (addAll
                    (into-array Node [circle1 circle2 line]))))
        root (doto (new Group)
               (.. getChildren (addAll (into-array Node [canvas overlay]))))
        stage (doto (new Stage)
                (.setScene (new Scene root 800 600)))]
    stage))
