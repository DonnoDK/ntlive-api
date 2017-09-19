(ns ntlive-api.core
  (:gen-class)
  (:require [net.cgrand.enlive-html :as html]
            [org.httpkit.client :as http]
            [clojure.string :as str]
            [clojure.data.json :as json]
            [compojure.core :as compojure]
            [ring.middleware.defaults :as ring]
            [ring.adapter.jetty :as jetty]))

(defn string->int[s]
  (some->> s (re-find #"-*\d+") Integer.))

(defn extract-results
  ([parser-cfg] (extract-results parser-cfg nil))
  ([{:keys [url selectors parser]} arg]
   (map parser
        (html/select
         (html/html-snippet (:body @(http/get (str "http://ntlive.dk/rt/" url arg))))
         selectors))))

(def parsers
  {:times  {:parser (fn [elm] (let [time-str (-> elm :content last (str/replace #"\.{3} |[\n\t]*" ""))
                                    [hour-minute offset] (str/split time-str #"\+|\*")
                                    [hour minute] (map string->int (-> hour-minute (str/split #"\.")))]
                                {:line        (-> elm :content first (str/split #" mod ") first)
                                 :destination (-> elm :content first (str/split #" mod ") last)
                                 :time {:hour   hour
                                        :minute minute
                                        :offset (or (string->int offset) 0)}
                                 :reliable?   (-> time-str (str/includes? "*") not)}))
            :selectors [:li]
            :url "destination/"}
   
   :stops  {:parser (fn [elm] {:name (-> elm :content first :content first)
                               :id   (-> elm :content first :attrs :href (str/split #"/") last)})
            :selectors [:li]
            :url "stop/"}
   
   :routes {:parser (fn [elm] (let [e (-> elm :content first)]
                                {:name (-> e :content first (str/split #": ") last)
                                 :id   (-> e :attrs :href (str/split #"/") last)
                                 :line (-> e :content first (str/split #": ") first)}))
            :selectors [:li]
            :url "route?id="}

   :cities {:parser (fn [elm] {:name (-> elm :content first str/trim)
                               :id   (-> elm :attrs :value)})
            :selectors [:option]
            :url "index"}})

(compojure/defroutes routes
  (compojure/GET "/cities" []
                 {:headers {"Content-Type" "application/json; charset=utf-8"}
                  :body 
                  (json/write-str (extract-results (:cities parsers)))})
  (compojure/GET "/city/:cityid" [cityid]
                 {:headers {"Content-Type" "application/json; charset=utf-8"}
                  :body 
                  (json/write-str (extract-results (:routes parsers) cityid))})
  (compojure/GET "/city/:cityid/:routeid" [cityid routeid]
                 {:headers {"Content-Type" "application/json; charset=utf-8"}
                  :body 
                  (json/write-str (extract-results (:stops parsers) routeid))})
  (compojure/GET "/city/:cityid/:routeid/:stopid" [cityid routeid stopid]
                 {:headers {"Content-Type" "application/json; charset=utf-8"}
                  :body 
                  (json/write-str (extract-results (:times parsers) stopid))}))



(def app
  (ring/wrap-defaults routes ring/site-defaults))

(defn -main
  [& args]
  (jetty/run-jetty app {:port 8000}))
