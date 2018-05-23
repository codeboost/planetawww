(ns plawww.server
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.logging :refer [info warn error]]
            [config.core :refer [env]]
            [plawww.handler :refer [app]]
            [plawww.db.core :as db]
            [ring.adapter.jetty :refer [run-jetty]])

  (:gen-class))

(defrecord WebServer [opts]
  component/Lifecycle
  (start [this]
    (info "Starting WebServer.")
    (if (:http-server this)
      this
      (assoc this :http-server (run-jetty #'app opts))))

  (stop [this]
    (info "Stopping WebServer.")
    (when-let [server (:http-server this)]
      (.stop server))
    this))

(defn web-server
  "Returns a new instance of the WebServer component"
  [opts]
  (WebServer. opts))


(defn system [{:keys [db-options web-server-options]}]
  (component/system-map
   :database (db/new-database db-options)
   :web-server (web-server web-server-options)))

(defn -main [& args]
  (let [options {:db-options
                 {:server "//localhost:5432"
                  :user "pm"
                  :password nil
                  :app-name "plan"}
                 :web-server-options {:port (Integer/parseInt (or (env :port) "3000"))}}]
    (component/start (system options))))

