(ns plawww.db.core
  (:require
   [clojure.java.jdbc :as j]
   [config.core :refer [env]]
   [honeysql.core :as hsql]))



(def mysql-uri (format
                "mysql://root:%s@mysql:3306/mediacore?charset=utf8&use_unicode=0"
                (env :planeta-mysql-pwd)))

(defn get-media-hsql-unfinished []
  (let [mfq (hsql/build :select [:media_id
                                 :unique_id
                                 :unique_id :filename
                                 :type
                                 :size
                                 :width
                                 :height]
                        :from [:media_files :mf])
        mtq (hsql/build :select []
                        :from [:media_tags :mt])
        sql (hsql/build
              :select [:id
                       :mf.type
                       :slug
                       :publish_on
                       :title
                       :subtitle
                       :description
                       :description_plain
                       :duration
                       :views
                       :media_id
                       :tags
                       :unique_id
                       :filename
                       :size
                       :width
                       :height]
              :from :media
              ; media_id, unique_id, unique_id as filename, type, size,  width, height FROM media_files
              :inner-join [])]

    (j/query mysql-uri (hsql/format sql))))

(def get-media-sql-str
  "SELECT id, mf.type, slug, publish_on, title, subtitle, description, description_plain, duration, views, tags, filename, size, width, height, unique_id FROM media
  INNER JOIN (SELECT media_id, unique_id, unique_id as filename, type, size,  width, height FROM media_files) AS mf ON mf.media_id = id
  LEFT JOIN (SELECT media_id, GROUP_CONCAT(tags.name SEPARATOR ', ') AS tags FROM media_tags INNER JOIN tags ON tag_id = tags.id GROUP BY media_id) AS mt ON mt.media_id = id;")


(defn get-media []
  (j/query mysql-uri get-media-sql-str))


(comment
 (get-media))
