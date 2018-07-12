(ns plawww.db.core
  (:require
   [clojure.java.jdbc :as j]
   [config.core :refer [env]]
   [honeysql.core :as hsql]))



(def mysql-uri (format
                "mysql://root:%s@mysql:3306/mediacore?charset=utf8&use_unicode=0"
                (env :planeta-mysql-pwd)))

(defn get-media-hsql []
  (let [mfq (hsql/build :select [:media_id
                                 :unique_id
                                 [:unique_id :filename]
                                 :type
                                 :size
                                 :width
                                 :height
                                 :storage_id]
                        :from [:media_files])
        mtq (hsql/build
             {:select [:media_id
                       [(hsql/raw "GROUP_CONCAT(t.name SEPARATOR ', ')") :tags]]
              :from [[:media_tags :mt]]
              :join [[:tags :t]
                     [:= :t.id :mt.tag_id]]
              :group-by [:media_id]})
        sql (hsql/build
             {:select [:id
                       :mf.type
                       :slug
                       :publish_on
                       :title
                       :subtitle
                       :description
                       :description_plain
                       :duration
                       :views
                       :mt.media_id
                       :tags
                       :unique_id
                       :filename
                       :size
                       :width
                       :height]
              :from :media
              :join [[mfq :mf] [:= :mf.media_id :id]]
              :left-join [[mtq :mt] [:= :mt.media_id :id]]})]

    (j/query mysql-uri (hsql/format sql))))

(def get-media-sql-str
  "SELECT id, mf.type, slug, publish_on, title, subtitle, description, description_plain, duration, views, tags, filename, size, width, height, unique_id
  FROM media
  INNER JOIN
    (SELECT media_id, unique_id, unique_id as filename, type, size,  width, height FROM media_files) AS mf ON mf.media_id = id
  LEFT JOIN
    (SELECT media_id, GROUP_CONCAT(tags.name SEPARATOR ', ') AS tags FROM media_tags
     INNER JOIN tags ON tag_id = tags.id GROUP BY media_id) AS mt ON mt.media_id = id;")


(defn get-media []
  (get-media-hsql))


(comment
 (get-media-hsql-unfinished))
