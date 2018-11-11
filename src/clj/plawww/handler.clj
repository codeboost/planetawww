(ns plawww.handler
  (:require [compojure.core :refer [GET defroutes] :as compojure]
            [clojure.tools.logging :refer [info error]]
            [compojure.route :refer [not-found resources]]
            [hiccup.page :refer [include-js include-css html5 html4]]
            [plawww.middleware :refer [wrap-middleware]]
            [plawww.db.core :as db]
            [ring.middleware.file :refer [wrap-file]]
            [plawww.partial :refer [wrap-partial-content]]
            [config.core :refer [env]]
            [clojure.data.json :as json]))

(defn media-path [& [file]]
  (let [data-path (or (env :planeta-mediadrop-data) "../planeta-data/mediadrop")
        ret-path (if file (str data-path file) data-path)]
    ret-path))

(defn load-db-data
  "Loads the media info from database.
  Returns a map, containing:
    :media - published media items.
    :categories - media categories."
  []
  {:media (db/get-media)
   :categories (db/get-categories)})

(def mount-target
  [:div#app
   [:h3 "Nu ti graghi..."]])

(defn google-analytics-include []
  [:script {:async true
            :src "https://www.googletagmanager.com/gtag/js?id=UA-128602722-1"}])

(defn google-analytics-init []
  [:script
   "window.dataLayer = window.dataLayer || [];\n
  function gtag(){dataLayer.push(arguments);}\n
  gtag('js', new Date());
  gtag('config', 'UA-128602722-1');"])

(defn head
  "Head tag for the website, including css and scripts required by the app.
  The head also contains the media database, which should be accessible on the client
  as `kolbasulPlanetar` global javascript variable."
  [css-includes]
  (let [db-data (load-db-data)
        db-json (json/write-str db-data)]
    [:head
     [:title "Planeta Moldova"]
     [:meta {:charset "utf-8"}]
     [:meta {:name    "viewport"
             :content "width=device-width, initial-scale=1"}]
     (map (fn [css-include]
            (include-css css-include)) css-includes)
     [:script (str "var kolbasulPlanetar = " db-json ";")]
     (google-analytics-include)
     (google-analytics-init)]))

(defn main-page []
  (html5
    (head ["/css/crt/crt.css"
           "/css/animations.css"])
    [:body {:class "body-container"}
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
  (GET "/pragoane*" [] (main-page))
  (GET "/text*" [] (main-page))
  (GET "/carti*" [] (main-page))
  (GET "/settings*" [] (main-page))
  (GET "/colectii*" [] (main-page))
  (resources "/")
  (compojure/context "/data" []
    (->
     (not-found "File Not Found")
     (wrap-file (media-path))
     (wrap-partial-content)))

  (not-found "Not Found"))

(def app (wrap-middleware #'routes))
