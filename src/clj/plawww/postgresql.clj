(ns plawww.postgresql
  (:import
   [org.postgresql.copy PGCopyOutputStream]
   [org.postgresql.util PGobject]
   [org.postgresql PGConnection])
  (:require
   [cheshire.core :as json]
   [clojure.java.jdbc :as j]
   [clojure.string :as string]))

(deftype Json [v])

(defn as-json [v] (Json. v))

(defn value->json-pgobject [value]
  (doto (PGobject.)
    (.setType "json")
    (.setValue (json/generate-string value))))

(extend-protocol j/ISQLValue
  Json
  (sql-value [value] (value->json-pgobject (.v value))))

(extend-protocol j/IResultSetReadColumn
  Array
  (result-set-read-column [pgobj metadata idx]
    (seq (.getArray pgobj)))

  PGobject
  (result-set-read-column [pgobj metadata idx]
    (let [type  (.getType pgobj)
          value (.getValue pgobj)]
      (case type
        ("json" "jsonb") (json/parse-string value true)
        value))))



