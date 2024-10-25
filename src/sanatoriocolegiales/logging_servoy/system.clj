(ns sanatoriocolegiales.logging-servoy.system
  "Service component lifecycle management"
  (:gen-class)
  (:require
   ;; Application dependencies
   [sanatoriocolegiales.logging-servoy.router :as router]
   ;; Component system
   [donut.system :as donut]
   ;; System dependencies
   [org.httpkit.server     :as http-server]
   [com.brunobonacci.mulog :as µ]
   [datomic.api :as d])
  (:import java.io.IOException))

(def main
  "System Component management with Donut"
  {::donut/defs
   ;; Option: move :env data to resources/config.edn and parse with aero reader
   {:env
    {:app-version "0.1.0"
     :app-env "prod"
     :http-port (or (System/getenv "SERVICE_HTTP_PORT") 3000)
     :persistence
     {:datomic-conn-string (System/getenv "DATOMIC")}}
    ;; mulog publisher for a given publisher type, i.e. console, cloud-watch
    :db 
    {:datomic 
     #::donut{:start (fn conexion-datomic
                       [{{:keys [conn-str]} ::donut/config}]
                       (try
                         (µ/log ::estableciendo-conexion-datomic)
                         (d/connect conn-str)
                         (catch IOException e (µ/log ::error-conexion-datomic :mensaje (ex-message e)))))
              :stop (fn interrumpir-conexion-datomic
                      [{::donut/keys [instance]}]
                      (try 
                        (µ/log ::liberando-conexion-datomic)
                        (d/release instance)
                        (catch IOException e (µ/log ::error-al-liberar-conexion-datomic :mensaje (ex-message e)))))
              :config {:conn-str (donut/ref [:env :persistence :datomic-conn-string])}}            
    }
    :event-log
    {:publisher
     #::donut{:start (fn mulog-publisher-start
                       [{{:keys [global-context publisher]} ::donut/config}]
                       (µ/set-global-context! global-context)
                       (µ/log ::log-publish-component
                                  :publisher-config publisher
                                  :local-time (java.time.LocalDateTime/now))
                       (µ/start-publisher! publisher))

              :stop (fn mulog-publisher-stop
                      [{::donut/keys [instance]}]
                      (µ/log ::log-publish-component-shutdown :publisher instance :local-time (java.time.LocalDateTime/now))
                      ;; Pause so final messages have chance to be published
                      (Thread/sleep 250)
                      (instance))

              :config {:global-context {:app-name "sanatoriocolegiales logging-servoy service" 
                                        :version (donut/ref [:env :app-version])
                                        :environment (donut/ref [:env :app-env])}
                       ;; Publish events to console in json format
                       ;; optionally add `:transform` function to filter events before publishing
                       :publisher {:type :console-json 
                                   :pretty? false 
                                   #_#_:transform identity}}}}

    ;; HTTP server start - returns function to stop the server
    :http
    {:server
     #::donut{:start (fn http-kit-run-server
                       [{{:keys [handler options]} ::donut/config}]
                       (µ/log ::http-server-component
                                  :handler handler
                                  :port (options :port)
                                  :local-time (java.time.LocalDateTime/now))
                       (http-server/run-server handler options))

              :stop  (fn http-kit-stop-server
                       [{::donut/keys [instance]}]
                       (µ/log ::http-server-component-shutdown
                                  :http-server-instance instance
                                  :local-time (java.time.LocalDateTime/now))
                       (instance))

              :config {:handler (donut/local-ref [:handler])
                       :options {:port  (donut/ref [:env :http-port])
                                 :join? true}}}

     ;; Function handling all requests, passing system environment
     ;; Configure environment for router application, e.g. database connection details, etc.
     :handler (router/app (donut/ref [:db]))}}})

(comment
  
  (d/create-database "datomic:sql://bases_auxiliares?jdbc:postgresql://10.200.0.190:5432/bases_auxiliares?user=auxiliar&password=auxi2013")
  
  )