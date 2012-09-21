(ns reform.main
  (:use [compojure.handler :only [api]]
        [compojure.core :only [GET POST defroutes]]
        [ring.adapter.jetty :only [run-jetty]]
        [ring.middleware.cookies :only [wrap-cookies]]
        [ring.util.response :only [response content-type]])
  (:require [cheshire.core :as json]))

(def treatments
  [{:color "red"
    :text  "Sign Up For Free!"}
   {:color "blue"
    :text  "Sign Up!"}
   {:color "yellow"
    :text "DONT SIGN UP"}])

(let [next (atom (cycle (range (count treatments))))]
  (defn next-treatment
    []
    (let [res (first @next)]
      (swap! next rest)
      res)))

(defn jsonp-response
  [body callback]
  (let [body (str callback "(" (json/encode body) ");")]
    (-> body
        response
        (content-type "text/javascript"))))

(defn wrap-request-logging
  [handler]
  (fn [{:keys [uri query-string cookies] :as req}]
    (locking System/out (println "request:" uri query-string "cookies:" cookies))
    (handler req)))

(defn get-treatment
  [{cookies :cookies {callback :callback} :params}]
  (if-let [{value :value} (cookies "treatment")]
    (jsonp-response (nth treatments (Integer. value)) callback)
    (-> (jsonp-response (nth treatments (next-treatment)) callback)
        (assoc-in [:cookies :treatment] treatment))))

(defroutes api-routes
  (GET "/" req (get-treatment req)))

(def app (-> #'api-routes
             wrap-request-logging
             wrap-cookies
             api))

(defn -main
  [port]
  (run-jetty app {:port (Integer. port)}))