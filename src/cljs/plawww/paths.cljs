;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.paths
  (:require [clojure.string :as str]
            [cemerick.url :as url]))

(def AUDIO_IMAGE_PATH "/images/planeta.png")
(def EXPLORER_PATH "/pragoane")
(def CATEGORIES_PATH "/colectii")

(defn category-image [name]
  (str "/data/images/categories/" name ".jpg"))

(defn s-image-path [id & [show-custom?]]
  (if-not (= true show-custom?)
    AUDIO_IMAGE_PATH
    (str "/data/images/media/" id "s.jpg")))

(defn l-image-path [id & [show-custom?]]
  (if-not (= true show-custom?)
    AUDIO_IMAGE_PATH
    (str "/data/images/media/" id "l.jpg")))

(defn media-image-path
  "Returns the path for the image for the media item identified by `id`.
  If category-name is specified, returns the image for the category.
  Otherwise, returns the default audio image path, unless `show-custom?` is true.
  If `show-custom?` is true, returns the actual media item image.
  Options:
    size - :thumbnail or :large
    category-name - if not nil, returns the image for the category.
    show-custom? - Return the actual item image path, instead of the default image (should be true for video items)."
  [id {:keys [size category-name show-custom?]
       :or {size :thumbnail
            category-name nil
            show-custom? false}}]
  (if-not (or (empty? category-name) show-custom?)
    (category-image category-name)
    (case size
      :thumbnail
      (s-image-path id show-custom?)
      (l-image-path id show-custom?))))

(defn media-path [filename]
  (str "/data/media/" filename))

(defn *-path [THE_PATH subpath]
  (let [subpath (str subpath)]
    (if-not (empty? subpath)
      (str THE_PATH "/" subpath)
      THE_PATH)))

(defn explorer-path
  "Returns the relative path to the media explorer.
  subpath must be a relative path (eg. not start with /).
  If subpath is empty, the root explorer path is returned."
  [subpath & [keep-query?]]
  (let [subpath (if keep-query?
                  (str subpath (.-search (.-location js/window)))
                  subpath)]
    (*-path EXPLORER_PATH subpath)))

(defn full-explorer-path [basename subpath]
  (str basename (explorer-path subpath)))

;Too many `path` functions, clean this up please, too lazy now.
(defn path-for-item-with-title
  "Returns the full path for the media item with title"
  [title]
  (explorer-path (js/encodeURIComponent (str/replace title " " "_") :keep-query)))

(defn current-url-query
  "Parse URL parameters into a hashmap"
  []
  (or (:query (url/url (-> js/window .-location .-href))) {}))

(defn tags-path
  [tags]
  (let [tags (if (string? tags) (str/split tags #"\+") tags)
        tags (if (coll? tags) (str/join "+" tags) tags)
        q (current-url-query)
        q (if-not (empty? tags) (assoc q "taguit" tags) q)
        query (str "?" (-> (url/map->query q) (str/replace "%2B" "+")))]
    (*-path EXPLORER_PATH query)))

(defn categories-path [subpath]
  (*-path CATEGORIES_PATH subpath))

(def storage-file 1)
(def storage-youtube 3)

(defn item-path [{:keys [unique_id storage_id filename]}]
  (if (= storage_id storage-youtube)
    (str "https://www.youtube.com/watch?v=" unique_id)
    (media-path filename)))