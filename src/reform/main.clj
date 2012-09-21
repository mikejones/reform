(ns reform.main
  (:use [compojure.handler :only [api]]
        [compojure.core :only [GET POST defroutes]]
        [ring.adapter.jetty :only [run-jetty]]
        [ring.util.response :only [response content-type]])
  (:require [cheshire.core :as json]))

(def treatments
  [{:selector "#sign-up"
    :treatment {:color "red"
                :text  "Sign Up For Free!"}}])

(defn jsonp-response
  [body callback]
  (let [body (str callback "(" (json/encode body) ");")]
    (-> body
        response
        (content-type "text/javascript"))))

(defn wrap-request-logging
  [handler]
  (fn [{:keys [uri query-string] :as req}]
    (locking System/out (println "request" uri query-string))
    (handler req)))

(defroutes api-routes
  (GET "/" [callback] (jsonp-response treatments callback)))

(def app (-> #'api-routes
             wrap-request-logging
             api))

(defn -main
  [port]
  (run-jetty app {:port (Integer. port)}))