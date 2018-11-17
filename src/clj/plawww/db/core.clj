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
        mcat (hsql/build
              {:select   [:media_id
                          [(hsql/raw "GROUP_CONCAT(c.id SEPARATOR ', ')") :categories]]
               :from     [[:media_categories :mc]]
               :join     [[:categories :c]
                          [:= :c.id :mc.category_id]]
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
                       :mf.storage_id
                       :categories]
              :from      :media
              :join      [[mfq :mf] [:= :mf.media_id :id]]
              :left-join [[mtq :mt] [:= :mt.media_id :id]
                          [mcat :mcat] [:= :mcat.media_id :id]]
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
  (let [hsql (hsql/build :select [:id :name :slug] :from :categories :order-by [:name])]
    (j/query mysql-uri (hsql/format hsql))))


(defn ->vector
  "Trims, then splits a comma-separated string into a vector."
  [value]
  (when value
    (mapv str/trim (str/split value #","))))

(defn massage-item
  "Remove nil values, trim tags and split them into arrays"
  [item]
  (into {}
        (remove
         nil?
         (map (fn [[key value]]
                (cond
                  (nil? value) nil
                  (= key :tags) [key (->vector value)]
                  (= key :categories) [key (mapv #(Integer/parseInt %) (->vector value))]
                  (= key :publish_on) [key (str (java.sql.Date. (.getTime value)))]
                  :else
                  [key value]))
              item))))


(defn massage-media-items [media-items]
  (mapv massage-item media-items))

(defn get-media []
  (massage-media-items (get-media-hsql)))

(defn- update-categories-based-on-tags
  "Service function"
  [tag-or-tags category-id]
  (let [tags (if (coll? tag-or-tags) (set tag-or-tags) #{tag-or-tags})
        vals (->>
              (get-media)
              (filter #(some tags (:tags %)))
              (map #(hash-map :media_id (:id %) :category_id category-id)))
        q (hsql/build :insert-into :media_categories
                      :values vals
                      :on-conflict []
                      :do-nothing [])]
    (hsql/format q :parameterizer :none)
    #_(j/execute! mysql-uri (hsql/format q))))


(defn- update-missing-categories
  "Service function.
  Inserts media-categories records for items that don't have any category, setting category-id to `category-id`.
  Returns the number of records affected or nil if there were no items without cateogry."
  [category-id]
  (let [q (hsql/build :select [:id :title]
                      :from [[:media :m]]
                      :left-join [[:media-categories :mc]
                                  [:= :mc.media_id :m.id]]
                      :where [:= :mc.category_id nil])
        without-cats (j/query mysql-uri (hsql/format q))]
    (when-not (empty? without-cats)
      (let [values (map #(hash-map :media_id % :category_id category-id) (map :id without-cats))
            q (hsql/build :insert-into
                          :media_categories
                          :values values)]
        (j/execute! mysql-uri (hsql/format q))))))


(comment
 (get-media)
 (update-missing-categories 16)
 (get-categories)
 (update-categories-based-on-tags ["febre39"] 21))



