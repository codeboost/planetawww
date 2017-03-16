(ns plawww.handler
  (:require [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [hiccup.page :refer [include-js include-css html5 html4]]
            [plawww.middleware :refer [wrap-middleware]]
            [config.core :refer [env]]))

(def mount-target
  [:div#app
      [:h3 "ClojureScript has not been compiled!"]
      [:p "please run "
       [:b "lein figwheel"]
       " in order to start the compiler"]])

(defn head [css-includes]
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   (map (fn[css-include]
          (include-css css-include)) css-includes)
   ])

(def classic-css [(if (env :dev) "/css/site.css" "/css/site.min.css")
                  "https://fonts.googleapis.com/css?family=Orbitron:700"])

(defn main-page [which-css]
  (html5
    (head which-css)
    [:body {:class "body-container"}
     mount-target
     (include-js "/js/app.js")]))

(defn cards-page [which-css]
  (html5
    (head which-css)
    [:body
     mount-target
     (include-js "/js/app_devcards.js")]))


(def crt-css ["/css/crt/crt.css"])

(defn crt-site [request]
  (main-page crt-css))


(defroutes routes
  (GET "/" [] (main-page crt-css))
  (GET "/about" [] (main-page crt-css))
  (GET "/cards" [] (cards-page classic-css))
           (GET "/crt" [] crt-site)

  (resources "/")
  (not-found "Not Found"))

(def app (wrap-middleware #'routes))
