(ns plawww.devel
  (:require
   [clojure.tools.namespace.repl :refer [refresh]]
   [com.stuartsierra.component :as component]
   [config.core :refer [env]]
   [plawww.server :as server]))

(def system nil)

(defn init []
  (let [options {:db-options
                 {:server "//localhost:5432"
                  :user "pm"
                  :password nil
                  :app-name "plan"}
                 :web-server-options {:port 3000
                                      :join? false}}]
    (alter-var-root #'system
                    (constantly (server/system options)))))

(defn start []
  (alter-var-root #'system component/start))

(defn stop []
  (alter-var-root #'system
                  (fn [s] (when s
                            (println "Stopping stuff.")
                            (component/stop s)))))

(defn go []
  (init)
  (start))

(defn reset []
  (stop)
  (refresh :after 'user/go))
