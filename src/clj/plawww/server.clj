(ns plawww.server
  (:require [plawww.handler :refer [app]]
            [config.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]]
            [plawww.db.core :as db])
  (:gen-class))

(defn connect-database [opts]
  (db/set-application-name! "plawww")
  (db/set-server! "//localhost:5432")
  (db/connect! "pm" (:db-pwd opts)))

(comment
  (connect-database {:db-user "postgres"}))

(defn -main [& args]

  (let [port (Integer/parseInt (or (env :port) "3000"))]
    (run-jetty app {:port port :join? false})))
