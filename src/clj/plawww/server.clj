(ns plawww.server
  (:require [plawww.handler :as handler :refer [app]]
            [config.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

(defn -main [& args]
  ;; Fail fast if the catalog is missing or unparseable.
  (try
    (handler/load-db-data)
    (catch Exception e
      (throw (ex-info (str "Cannot load catalog: " (handler/media-path "/catalog.json"))
                      {:path (handler/media-path "/catalog.json")}
                      e))))

  ;; yogthos/config yields numbers for numeric env values (eg. PORT=3444),
  ;; and strings otherwise - accept both.
  (let [port (or (env :port) 3333)
        port (if (string? port) (Integer/parseInt port) port)]
    (run-jetty app {:port port :join? false})))
