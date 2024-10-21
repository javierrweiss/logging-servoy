;; ---------------------------------------------------------
;; sanatoriocolegiales.logging-servoy
;;
;; TODO: Provide a meaningful description of the project
;;
;; Start the service using donut system configuration
;; defined in `system.clj`
;;
;; The service consist of
;; - httpkit web application server
;; - metosin/reitit for routing and ring for request / response management
;; - mulog event logging service
;;
;; Related namespaces
;; `sanatoriocolegiales.logging-servoy/system` donut system configuration
;; ---------------------------------------------------------


(ns sanatoriocolegiales.logging-servoy.service
  "Gameboard service component lifecycle management"
  (:gen-class)
  (:require
   ;; Component system
   [donut.system           :as donut]
   [sanatoriocolegiales.logging-servoy.system :as system]))


;; --------------------------------------------------
;; Service entry point

(defn -main
  "sanatoriocolegiales logging-servoy service managed by donut system,
  Aero is used to configure the donut system configuration based on profile (dev, test, prod),
  allowing environment specific configuration, e.g. mulog publisher
  The shutdown hook gracefully stops the service on receipt of a SIGTERM from the infrastructure,
  giving the application 30 seconds before forced termination."
  []
  (let [profile (or (keyword (System/getenv "SERVICE_PROFILE"))
                    :dev)

        ;; Reference to running system for shutdown hook
        running-system (donut/start (or (profile :profile) :prod))]
    
    ;; Shutdown system components on SIGTERM
    (.addShutdownHook
     (Runtime/getRuntime)
     (Thread. ^Runnable #(donut/signal running-system ::donut/stop)))))
;; --------------------------------------------------


;; --------------------------------------------------
;; Example clojure.exec function

(defn greet
  "Greeting message via Clojure CLI clojure.exec"
  ;; TODO: call greet with hash-map argument
  ([] (greet "secret engineering"))
  ([{:keys [team-name]}]
   (str "sanatoriocolegiales logging-servoy service developed by the " team-name " team")))


(comment
  ;; --------------------------------------------------
  ;; REPL workflow commands

  (greet {:team-name "Jrivero"}))

   ; End of rich comment
