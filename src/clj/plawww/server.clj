(ns plawww.server
  (:require [plawww.handler :refer [app]]
            [config.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

(defn -main [& args]
  (if-not (env :planeta-mediadrop-data)
    (println "PLANETA_MEDIADROP_DATA environment variable not set.")
    (let [port (Integer/parseInt (or (env :port) "3333"))]
      (run-jetty app {:port port :join? false}))))
