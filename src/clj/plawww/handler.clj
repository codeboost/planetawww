(ns plawww.handler
  (:require [compojure.core :refer [GET defroutes] :as compojure]
            [clojure.tools.logging :refer [info error]]
            [compojure.route :refer [not-found resources]]
            [hiccup.page :refer [include-js include-css html5 html4]]
            [plawww.middleware :refer [wrap-middleware]]
            [ring.middleware.file :refer [wrap-file]]
            [config.core :refer [env]]
            [clj-http.client :as http-client]))


(defonce db-json (atom nil))

(defn load-db-json! []
  (let [filename (str (env :planeta-mediadrop-data) "/db.json")
        _ (println "Loading " filename)
        file-contents (slurp filename)]
    (if-not file-contents
      (error "Could not load db json: " filename)
      (reset! db-json file-contents))))

(def mount-target
  [:div#app
   [:h3 "Nu ti graghi..."]])

(defn head [css-includes]
  (when-not @db-json
    (load-db-json!))
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

(defn main-page [which-css]
  (html5
    (head which-css)
    [:body {:class "body-container"}
     mount-target
     (include-js "/lib/soundmanager2-nodebug-jsmin.js")
     (include-js "/js/app.js")]))

(defn cards-page [which-css]
  (html5
    (head which-css)
    [:body
     mount-target
     (include-js "/js/app_devcards.js")]))


(def crt-css ["/css/crt/crt.css"
              "/css/animations.css"])

(defn crt-site [request]
  (main-page crt-css))

(defroutes routes
  (GET "/" [] (main-page crt-css))
  (GET "/menu*" [] (main-page crt-css))
  (GET "/media*" [] (main-page crt-css))
  (GET "/home*" [] (main-page crt-css))
  (GET "/about*" [] (main-page crt-css))
  (GET "/settings*" [] (main-page crt-css))
  (resources "/")
  (compojure/context "/data" []
    (-> (not-found "File Not Found")
        (wrap-file (env :planeta-mediadrop-data))))

  (not-found "Not Found"))

(def app (wrap-middleware #'routes))

