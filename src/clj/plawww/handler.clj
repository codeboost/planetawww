(ns plawww.handler
  (:require
   [clj-http.client :as http-client]
   [compojure.core :refer [defroutes]]
   [compojure.route :refer [not-found resources]]
   [compojure.api.sweet :refer [api context describe GET POST]]
   [config.core :refer [env]]
   [hiccup.page :refer [include-js include-css html5 html4]]
   [plawww.middleware :refer [wrap-middleware]]
   [ring.util.http-response :as responses]
   [plawww.db.core :as db]
   [plawww.db.band :as band]))

(def MEDIA_JSON "/Users/florinbraghis/code/yo/planeta/plawww/resources/public/db/results.json")


(def mount-target
  [:div#app
   [:h3 "Nu ti graghi..."]])

(defn head [css-includes]
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name    "viewport"
           :content "width=device-width, initial-scale=1"}]
   (map (fn [css-include]
          (include-css css-include)) css-includes)
   [:script
    (str "var kolbasulPlanetar = " (slurp MEDIA_JSON) ";")]])


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


;Make sure to increase the maximum number of media results in the media drop admin panel (Data API -> API Settings).
(def all-media-url "http://localhost:8080/api/media?api_key=U3rD1T5OiUt7FldwGTD&limit=4000")


(defn update-data []
  (try
    (let [server-response (http-client/get all-media-url)
          {:keys [status body]} server-response]
      (cond
        (not (= status 200)) {:status  "error"
                              :message (str "Server returned unknown result: " status)}
        :else (do
                (spit MEDIA_JSON body)
                {:status "success"})))

    (catch Exception e {:status "error"
                        :message (str "Could not complete data update: " (.getMessage e))})))


(defn build-api
  "Construct ring/swagger configurations/routes/middleware.
  Currently just an empty shell."
  [opts]
  (api
   (GET "/test" []
        {:summary "Service test"
         :description "Used to test if this thing is working."
         :tags ["status"]
         :responses {204 {:description "Success."}
                     503 {:description "Unsupported."}}}
        (responses/no-content))

   (context "/media" {:keys [band-db] :as request}
     (GET "/all" []
       {:summary "Get all the media entries"
        :description "Testing if the db thing is working."
        :tags ["media"]
        :responses {200 {:description "Success."}}}
       (responses/ok (band/get-media band-db))))))






(defn api-routes
  ""
  [base-path]
  (-> (compojure.core/routes
        (context "/v1" [] (build-api {:swagger-context (str base-path "/v1")})))))


(defn www-routes []
  (compojure.core/routes
    (GET "/" [] (main-page crt-css))
    (GET "/menu*" [] (main-page crt-css))
    (GET "/media*" [] (main-page crt-css))
    (GET "/test/*" [] (main-page crt-css))
    (GET "/puzzle/*" [] (main-page crt-css))
    (GET "/cards" [] (cards-page classic-css))
    (GET "/crt" [] crt-site)
    (GET "/update-data" [] (update-data))))


(defroutes routes
  (context "/api" [] (api-routes "/api"))
  (context "/" [] (www-routes))
  (resources "/")
  (not-found "Unable to locate requested resource.\n"))

(def app (wrap-middleware #'routes))
