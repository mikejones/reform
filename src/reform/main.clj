(ns reform.main
  (:use [compojure.handler :only [api]]
        [compojure.core :only [GET POST defroutes]]
        [ring.adapter.jetty :only [run-jetty]]
        [ring.util.response :only [response content-type]])
  (:require [cheshire.core :as json]))

(def treatments
  {})

(defn jsonp-response
  [body jsonp]
  (let [body (str jsonp "(" (json/encode body) ");")]
    (-> body    
        response
        (content-type "application-json"))))

(defroutes api-routes
  (GET "/" [jsonp] (jsonp-response treatments jsonp)))

(def app (-> #'api-routes
             api))

(defn -main
  [port]
  (run-jetty app {:port (Integer. port)}))