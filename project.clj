(defproject farg/x "0.1.0-SNAPSHOT"
  :description "Experimental code for FARG projects"
  :url "https://github.com/bkovitz/x"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[better-cond "2.0.1-SNAPSHOT"]
                 [com.rpl/specter "1.1.0"]
                 [farg/util "0.1.0-SNAPSHOT"]
                 [farg/with-state "0.0.1-SNAPSHOT"]
                 [org.clojure/clojure "1.9.0"]
                 [ubergraph "0.4.0"]]
  :repl-options {:init (do
    (use 'farg.x.javafx)
    (javafx.embed.swing.JFXPanel.)
    (. javafx.application.Platform setImplicitExit false))})
