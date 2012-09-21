(ns reform.main
  (:use [compojure.handler :only [api]]
        [compojure.core :only [GET POST defroutes]]
        [ring.adapter.jetty :only [run-jetty]]
        [ring.util.response :only [response content-type]])
  (:require [cheshire.core :as json]))

(def treatments
  {})

(defn json-response
  [body]
  (-> body
      json/encode
      response
      (content-type "application-json")))

(defroutes api-routes
  (GET "/" [] (json-response treatments)))

(def app (-> #'api-routes
             api))

(defn -main
  [port]
  (run-jetty app {:port (Integer. port)}))