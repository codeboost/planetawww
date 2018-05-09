(ns plawww.server
  (:require [com.stuartsierra.component :as component]
            [config.core :refer [env]]
            [plawww.handler :refer [app]]
            [plawww.db.core :as db]
            [ring.adapter.jetty :refer [run-jetty]])

  (:gen-class))

(defn connect-database [opts]
  (db/set-application-name! "plawww")
  (db/set-server! "//localhost:5432")
  (db/connect! "pm" (:db-pwd opts)))

(comment
  (connect-database {:db-user "postgres"}))

(defn system [{:keys [db-server db-username db-password port]}]
  (component/system-map
   :database (db-component/new-database :app-name "Planeta"
                                        :server db-server
                                        :username db-username
                                        :password db-password)
   :scheduler nil
   :eventbus nil
   :web-server (run-jetty app {:port port :join? false})))

(defn -main [& args]

  (let [port (Integer/parseInt (or (env :port) "3000"))]
    (run-jetty app {:port port :join? false})))
