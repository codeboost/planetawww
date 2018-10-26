(ns plawww.handler
  (:require [compojure.core :refer [GET defroutes] :as compojure]
            [clojure.data.json :as json]
            [clojure.tools.logging :refer [info error]]
            [compojure.route :refer [not-found resources]]
            [hiccup.page :refer [include-js include-css html5 html4]]
            [plawww.middleware :refer [wrap-middleware]]
            [plawww.db.core :as db]
            [ring.middleware.file :refer [wrap-file]]
            [plawww.partial :refer [wrap-partial-content]]
            [config.core :refer [env]]
            [clj-http.client :as http-client]
            [clojure.string :as str]))


(defonce db-json (atom nil))

(defn media-path [& [file]]
  (let [data-path (or (env :planeta-mediadrop-data) "../planeta-data/mediadrop")
        ret-path (if file (str data-path file) data-path)]
    ret-path))

(defn my-value-writer [key value]
  (if (= key :publish_on)
    (str (java.sql.Date. (.getTime value)))
    value))


(defn ->tagv [value]
  (when value
    (mapv str/trim
      (-> value
          (str/split #",")))))

(defn massage-item [item]
  (into {}
    (remove
     nil?
     (map (fn [[key value]]
            (cond
              (nil? value) nil
              (= key :tags) [key (->tagv value)]
              :else
              [key value]))
          item))))

(defn massage-results [results]
  (mapv massage-item results))

(defn load-db-data! []
  (let [results (db/get-media)
        results (massage-results results)]
    (reset! db-json (json/write-str results
                                    :value-fn my-value-writer))))

(def mount-target
  [:div#app
   [:h3 "Nu ti graghi..."]])

(def ^:dynamic *reload-db-always* true)

(defn head [css-includes]
  (when (or (not @db-json) *reload-db-always*)
    (load-db-data!))
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name    "viewport"
           :content "width=device-width, initial-scale=1"}]
   (map (fn [css-include]
          (include-css css-include)) css-includes)
   [:script
    (str "var kolbasulPlanetar = " @db-json ";")]])


(def classic-css [(if (env :dev) "/css/site.css" "/css/site.min.css")
                  "https://fonts.googleapis.com/css?family=Orbitron:700"])

(defn main-page []
  (html5
    (head ["/css/crt/crt.css"
           "/css/animations.css"])
    [:body {:class "body-container"
            :scroll :no}
     mount-target
     (include-js "/js/app.js")
     (include-js "/lib/oscilloscope.js")]))

(defroutes routes
  (GET "/" [] (main-page))
  (GET "/menu*" [] (main-page))
  (GET "/media*" [] (main-page))
  (GET "/home*" [] (main-page))
  (GET "/about*" [] (main-page))
  (GET "/barul*" [] (main-page))
  (GET "/explorer*" [] (main-page))
  (GET "/text*" [] (main-page))
  (GET "/carti*" [] (main-page))
  (GET "/settings*" [] (main-page))
  (resources "/")
  (compojure/context "/data" []
    (->
     (not-found "File Not Found")
     (wrap-file (media-path))
     (wrap-partial-content)))

  (not-found "Not Found"))

(def app (wrap-middleware #'routes))

(comment
 (def age 8)
 (def names #{"Julia" "Rita" "Novak" "Bea" "Joana" "Alan"})

 (set names)


 (:name {:name "beatriz"
         :age 8
         :hair-color "brown"}))