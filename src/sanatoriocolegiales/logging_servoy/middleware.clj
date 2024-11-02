(ns sanatoriocolegiales.logging-servoy.middleware
  (:require
   [reitit.ring.middleware.exception :as exception]
   [com.brunobonacci.mulog :as mulog])
  (:import java.time.LocalDateTime))

(defn handler
  [mensaje _ _]
  mensaje)

(def exception-middleware
  (exception/create-exception-middleware
   (merge
    exception/default-handlers
    {::excepcion-persistencia (partial handler {:status 500
                                                :body "Hubo un error al persistir la información en la base de datos"}) 
     ::argumento-ilegal (partial handler {:status 400
                                          :body "Argumento no permitido"})
     ::exception/wrap (fn [handler e request]
                        (mulog/log ::excepcion-en-solicitud :mensaje (ex-message e) :fecha (LocalDateTime/now) :solicitud request)
                        (handler e request))}))) 


(defn wrap-trace-events
  "Log event trace for each api event with mulog/log."
  [handler id]
  (fn [request]
    ;; Add context of each request to all trace events generated for the specific request
    (mulog/with-context
     {:uri            (get request :uri)
      :request-method (get request :request-method)}

     ;; track the request duration and outcome
     (mulog/trace :io.redefine.datawarp/http-request
                  ;; add key/value pairs for tracking event only
                  {:pairs [:content-type     (get-in request [:headers "content-type"])
                           :content-encoding (get-in request [:headers "content-encoding"])
                           :middleware       id]
                   ;; capture http status code from the response
                   :capture (fn [{:keys [status]}] {:http-status status})}

                  ;; call the request handler
                  (handler request)))))

;; --------------------------------------------------
