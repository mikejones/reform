(ns reform.main
  (:use [compojure.handler :only [api]]
        [compojure.core :only [GET POST defroutes]]
        [ring.adapter.jetty :only [run-jetty]]
        [ring.middleware.cookies :only [wrap-cookies]]
        [ring.util.response :only [response content-type file-response header]])
  (:require [cheshire.core :as json]))

(def treatments
  [{:color "red"
    :text  "Sign Up For Free!"}
   {:color "blue"
    :text  "Sign Up!"}
   {:color "yellow"
    :text "DONT SIGN UP"}])

(def state
  (atom {:treated (vec (repeat (count treatments) 0))
         :completed (vec (repeat (count treatments) 0))}))

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
    (let [treatment (Integer. value)]
      (jsonp-response (nth treatments treatment) callback))
    (let [treatment (next-treatment)]
      (swap! state update-in [:treated treatment] inc)
      (-> (jsonp-response (nth treatments treatment) callback)
          (assoc-in [:cookies :treatment] treatment)))))

(defn complete
  [{cookies :cookies}]
  (when-not (cookies "completed")
    (let [{value :value} (cookies "treatment")
          treatment (Integer. value)]
      (swap! state update-in [:completed treatment] inc)))
  (-> (file-response "blank.gif")
      (content-type "image/gif")
      (header "Cache-Control" "private, no-cache, no-cache = Set-Cookie, proxy-revalidate")
      (header "Pragma" "no-cache")
      (assoc-in [:cookies :completed] true)))

(defroutes api-routes
  (GET "/" req (get-treatment req))
  (GET "/blank.gif" req (complete req))
  (GET "/state" [] (response (json/encode @state))))

(def app (-> #'api-routes
             wrap-request-logging
             wrap-cookies
             api))

(defn -main
  [port]
  (run-jetty app {:port (Integer. port)}))