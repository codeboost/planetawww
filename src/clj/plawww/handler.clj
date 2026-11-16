(ns plawww.handler
  (:require
    [clojure.data.json :as json]
    [clojure.string :as str]
    [clojure.tools.logging :refer [info error]]
    [clojure.java.io :as io]
    [compojure.core :refer [GET defroutes] :as compojure]
    [compojure.route :refer [not-found resources]]
    [config.core :refer [env]]
    [hiccup.page :refer [include-js include-css html5 html4]]
    [plawww.middleware :refer [wrap-middleware]]
    [plawww.partial :refer [wrap-partial-content]]
    [ring.middleware.file :refer [wrap-file]]))


(defn media-path [& [file]]
  (let [data-path (or (env :planeta-mediadrop-data) "../planeta-data/mediadrop")
        ret-path (if file (str data-path file) data-path)]
    ret-path))

(defn load-db-data
  "Loads the media catalog from `catalog.json` in the media data directory.
  Returns a map, containing:
    :media - published media items.
    :categories - media categories."
  []
  (json/read-str (slurp (media-path "/catalog.json")) :key-fn keyword))

(def splash-messages ["Se determina coordonatele..."
                      "Amush..."
                      "Asteptati oleaca..."
                      "Shezi ghinishor ca nu-ti fac nica.."
                      "Se incarca..."
                      "Se executa cateva sute de miliarde de instructiuni, asteptati."
                      "Se hraneste procesorul... "
                      "Incarc hash map-ul..."
                      "Pragoanele se apropie..."])

(def mount-target
  [:div#app
   [:h3 (rand-nth splash-messages)]])

(defn head
  "Head tag for the website, including css and scripts required by the app.
  The head also contains the media database, which should be accessible on the client
  as `kolbasulPlanetar` global javascript variable."
  [css-includes]
  (let [db-data (load-db-data)
        ;; <-escape so catalog content can never contain a literal
        ;; </script> sequence that terminates the inline script tag.
        db-json (str/replace (json/write-str db-data) "<" "\\u003c")]
    [:head
     [:title "Planeta Moldova"]
     [:meta {:charset "utf-8"}]
     [:meta {:name    "viewport" :content "width=device-width, initial-scale=1"}]
     [:meta {:name "description" :content "Site-ul oficial Planeta Moldova. Scenete audio, clipuri video si muzicile noastre."}]
     [:meta {:name "author" :content "2p-trip"}]
     (map include-css css-includes)
     [:script (str "var kolbasulPlanetar = " db-json ";")]]))

(defn main-page []
  (html5 {:lang "ro"}
    (head [(if (= true (:dev env)) "/css/crt/crt.css" "/css/crt/crt.min.css")])
    [:body {:class "body-container"}
     mount-target
     (include-js "/js/app.js")]))

(defn show-version []
  (slurp (io/resource ".semver")))

(defroutes routes
  (GET "/version" [] (show-version))
  (resources "/")
  (compojure/context "/data" []
    (->
     (not-found "File Not Found")
     (wrap-file (media-path))
     (wrap-partial-content)))
  (GET "/*" [] (main-page))

  (not-found "Not Found"))

(def app (wrap-middleware #'routes))
