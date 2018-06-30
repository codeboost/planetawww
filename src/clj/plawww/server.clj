(ns plawww.server
  (:require [plawww.handler :refer [app]]
            [config.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

(defn missing-env-vars []
  (cond-> []
    (not (env :planeta-mysql-pwd))
    (conj "PLANETA_MYSQL_PWD")))
    ;(not (env :planeta-mediadrop-data))
    ;(conj "PLANETA_MEDIADROP_DATA")))

(defn -main [& args]
  (let [missing-vars (missing-env-vars)]
    (when (seq missing-vars)
      (throw (ex-info "Missing environment variables" {:missing missing-vars}))))

  (let [port (Integer/parseInt (or (env :port) "3333"))]
    (run-jetty app {:port port :join? false})))
