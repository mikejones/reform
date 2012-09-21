(ns reform.main
  (:use [compojure.handler :only [api]]
        [compojure.core :only [GET POST defroutes]]
        [ring.adapter.jetty :only [run-jetty]]))

(defroutes api-routes
  (GET "/" [] "HI!"))

(def app (-> #'api-routes
             api))

(defn -main
  [port]
  (run-jetty app {:port (Integer. port)}))