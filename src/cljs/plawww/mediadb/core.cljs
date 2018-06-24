(ns plawww.mediadb.core
  (:require [clojure.string :as str]
            [plawww.utils :as utils]))

(defn search-match?
  "Returns true if `title` starts with `search-string`, regardless of case.
  If search string is empty, returns true."
  [title search-string]
  (or (str/blank? search-string)
      (str/starts-with?
       (str/lower-case title)
       (str/lower-case search-string))))

(defn unique-tags
  "Returns a set of unique tags extracted from the media items."
  [media-items]
  (->> media-items
       (map :tags)
       (apply concat)
       (map str/trim)
       (remove empty?)
       (set)))

(def unique-tags* (memoize unique-tags))

(defn equal-tag? [tag1 tag2]
  (= (str/trim (str/lower-case tag1))
     (str/trim (str/lower-case tag2))))

(defn items-for-tag [media-items tag]
  "Returns items from media-items which contain the tag `tag`."
  (filter (fn [{:keys [tags]}]
            (some #(equal-tag? tag %) tags)) media-items))

(defonce items-for-tag* (memoize items-for-tag))


(defn- text->tag
  "Make a tag struct from a tag title.
  The returned tag structure contains the following keys:
    :title     - title of the tag.
    :items     - a vector where each element is a media item from `media-items` which is tagged with `tag-title`.
    :num-items - number of elements in the items vector, for slightly faster sorts"
  [media-items tag-title]
  (let [items     (items-for-tag* media-items tag-title)
        num-items (count items)]
    {:title     tag-title
     :items     items
     :num-items num-items}))

(defn- group-by-tag
  "Groups media items from the `media-items` vector by tag.
  Returns a collection of tag structures."
  [media-items]
  (let [tags (unique-tags* media-items)]
    (mapv #(text->tag media-items %) tags)))

(defn by-tags [media-items]
  "Returns a list of Tag structures, each with following keys: [:title :items :num-items]"
  (sort-by
   :title
   (group-by-tag media-items)))

(defn- tag-titles
  "Returns a collection of tag strings from `items` that match (using `search-match?`) the string `s`."
  [items s]
  (->>
   (unique-tags* items)
   (filter #(search-match? % s))
   (set)))

(defn items-starting-with-letter
  "Returns the items in which `:title` starts with `letter`."
  [items letter]
  (filter #(utils/starts-with-letter? (:title %) letter) items))

(defn- filter-tagged
  "Filters tagged items (result of a `by-tags` call) leaving only those which have items with title that matches `s`."
  [items-by-tag items s]
  (let [titles (tag-titles items s)]
    (filter #(titles (:title %)) items-by-tag)))

(defn search-in-items [items s]
  (filter #(search-match? (:title %) s) items))