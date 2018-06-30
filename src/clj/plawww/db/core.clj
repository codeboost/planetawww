(ns plawww.db.core
  (:require
   [clojure.java.jdbc :as j]
   [config.core :refer [env]]
   [honeysql.core :as hsql]))

(def mysql-uri (format
                "mysql://root:%s@localhost:3306/mediacore"
                (env :planeta-mysql-pwd)))

(defn get-media []
  (let [sql (hsql/build
              :select :*
              :from :media)]
    (j/query mysql-uri (hsql/format sql))))

(comment
 (get-media))
