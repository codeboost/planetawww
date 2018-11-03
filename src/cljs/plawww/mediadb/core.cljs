;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.mediadb.core
  (:require [clojure.string :as str]
            [plawww.utils :as utils :refer [search-match?]]))



(defn unique-tags
  "Returns a set of unique tags extracted from the media items."
  [media-items]
  (->> media-items
       (map :tags)
       (apply concat)
       (map str/trim)
       (remove empty?)
       (into (sorted-set))))

(def unique-tags* (memoize unique-tags))

(defn items-for-tags
  "Returns only the items which are tagged with any tag from `set-of-tags`."
  [items set-of-tags]
  (if (empty? set-of-tags)
    items
    (filter
     (fn [{:keys [tags]}]
       (->> tags
            (map str/lower-case)
            (set)
            (clojure.set/intersection set-of-tags)
            (count)
            (pos?)))
     items)))

(defn items-for-tag [items tag]
  (items-for-tags items #{(str/lower-case tag)}))

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

(defn by-tags-which-start-with
  "Returns the tag items which have title starting with `s`.
  `tags` is a collection of {:title '' :items []} maps, presumably from a call to `by-tags`."
  [tags s]
  (filter
   (fn [{:keys [title]}] (search-match? title s))
   tags))

(defn search-in-items [items s]
  (filter #(search-match? (:title %) s) items))