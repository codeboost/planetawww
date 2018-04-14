(ns plawww.db.band
  (:require
   [honeysql.core :as hsql]
   [clojure.java.jdbc :as jdbc]))



(defn get-media [fdb]
  (->>
   {:select :title :from :media}
   hsql/build
   hsql/format
   (jdbc/query fdb)))


(comment
  (plawww.db.core/band-db 1)
  (get-media (plawww.db.core/band-db 1)))
