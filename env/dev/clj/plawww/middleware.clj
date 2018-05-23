(ns plawww.middleware
  (:require
   [plawww.db.core :as db]
   [prone.middleware :refer [wrap-exceptions]]
   [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
   [ring.middleware.reload :refer [wrap-reload]]))


(defn wrap-band-db
  "Add `band-db` database handle to the request."
  [handler]
  (fn [{:keys [identity] :as request}]
    (handler (assoc request :band-db (db/band-db 1)))))


(defn wrap-middleware [handler]
  (-> handler
      (wrap-defaults site-defaults)
      wrap-exceptions
      wrap-band-db
      wrap-reload))
