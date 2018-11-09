(ns plawww.db.core
  (:require
   [clojure.java.jdbc :as j]
   [config.core :refer [env]]
   [honeysql.core :as hsql]
   [clojure.string :as str]))

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
             {:select   [:media_id
                         [(hsql/raw "GROUP_CONCAT(t.name SEPARATOR ', ')") :tags]]
              :from     [[:media_tags :mt]]
              :join     [[:tags :t]
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
                       :height
                       :mf.storage_id]
              :from      :media
              :join      [[mfq :mf] [:= :mf.media_id :id]]
              :left-join [[mtq :mt] [:= :mt.media_id :id]]
              :where [:not [:= :publish_on nil]]})]

    (j/query mysql-uri (hsql/format sql))))

(defn get-comments [media-id]
  (let [q (hsql/build {:select [:subject :created_on :modified_on :author_name :author_email :body]
                       :from   :comments
                       :where  [:and
                                [:= :publishable true]
                                [:= :reviewed true]
                                [:= :media_id media-id]]})]
    (j/query mysql-uri (hsql/format q))))

(defn get-categories []
  (let [hsql (hsql/build :select [:id :name :slug] :from :categories)]
    (j/query mysql-uri (hsql/format hsql))))


(defn ->tagv
  "Splits a tag string into a vector of tags, using ',' as separator."
  [value]
  (when value
    (mapv str/trim
          (-> value
              (str/split #",")))))

(defn massage-item
  "Remove nil values, trim tags and split them into arrays"
  [item]
  (into {}
        (remove
         nil?
         (map (fn [[key value]]
                (cond
                  (nil? value) nil
                  (= key :tags) [key (->tagv value)]
                  (= key :publish_on) [key (str (java.sql.Date. (.getTime value)))]
                  :else
                  [key value]))
              item))))


(defn massage-media-items [media-items]
  (mapv massage-item media-items))

(defn get-media []
  (massage-media-items (get-media-hsql)))


(comment
 (map :publish_on (get-media))
 (get-categories))

