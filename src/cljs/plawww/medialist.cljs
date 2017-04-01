;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.medialist
  (:require [clojure.string :as str]
            [clojure.core.async :refer (chan put! <!)]
            [plawww.crt :refer [crt-page]]
            [cljsjs.typedjs]
            [clojure.string :as str]
            [reagent.core :as r]
            [plawww.media-item-detail :as media-item-detail]
            [plawww.search-component :refer [search-component]]
            [plawww.text-menu-component :refer [menu->hiccup]]))


(defn item->li [{:keys [title id]}]
  "Menu item to hiccup."
  (let [href (str "/media/" id)]
    ^{:key id} [:li [:a {:href href} title]]))


(defonce ALLMEDIA (js->clj js/kolbasulPlanetar :keywordize-keys true))

(def default-options {:group-by      :tag
                      :item-view-mode :plain
                      :dirty         true
                      :search-string ""
                      :expand-all?   false
                      :tags          #{}})


(defonce *display-options* (r/atom default-options))

(defn in? [coll el]
  (some #(= el %) coll))

(defn search-match? [title search-string]
  (or (str/blank? search-string)
      (str/starts-with?
        (str/lower-case title)
        (str/lower-case search-string))))

(defn group->menu [{:keys [title items num-items]} is-expanded? itemfn]
  (let [expanded? (r/atom is-expanded?)
        menu {:title title
              :items (map itemfn items)}]
    [menu->hiccup menu expanded?]))

;--------------- BY TAGS
(defn items-for-tag [media-items tag]
  "Returns items from media-items which contain the tag `tag`."
  (filter (fn [{:keys [tags]}]
            (some (fn [a-tag]
                    (= a-tag tag)) tags)) media-items))

(defn tags-from-items
  "Returns a set of unique tags extracted from the media items."
  [media-items]
  (->> media-items
       (map (fn [{:keys [tags]}] tags))
       (apply concat)
       (map str/trim)
       (set)))

(defn by-tags [media-items]
  "Returns a list of maps with following keys: [:tag :items :num-items]"
  (let [tags (tags-from-items media-items)]
    (reverse
      (sort-by
        :num-items
        (map (fn [tag]
               (let [items (items-for-tag media-items tag)
                     num-items (count items)
                     items items]
                 {:title     tag
                  :items     items
                  :num-items num-items})) tags)))))


(defonce BYTAG (by-tags (:media ALLMEDIA)))

;--------- BY LETTER ----------------------

(defn grouped-by-first-letter [media-items]
  (into (sorted-map) (group-by (fn [{:keys [title]}]
                                 (str/upper-case (first title))) media-items)))

(defn to-first-letter-groups [media-items]
  (let [g (grouped-by-first-letter media-items)
        groups (map (fn [first-letter items]
                      {:title first-letter
                       :items (sort-by :title items)}) (keys g) (vals g))]
    groups))

(defonce BYLETTER (to-first-letter-groups (:media ALLMEDIA)))

;-----------------------------------------


(defn render-by-tags [expand-all? itemfn]
  (let [tagged BYTAG
        menus (map #(group->menu % expand-all? itemfn) tagged)]
    (into [:div.media-items.horiz-container] menus)))

;-----------------------------

(defn search-in-items [media-items search-string]
  (filter (fn [{:keys [title]}]
            (search-match? title search-string)) media-items))

(defn render-by-letter [search-string expand-all? itemfn]
  (let [grouped (if (str/blank? search-string)
                  BYLETTER
                  (to-first-letter-groups
                    (search-in-items (:media ALLMEDIA) search-string)))
        should-expand? (or expand-all?
                           (not (str/blank? search-string)))
        results (map #(group->menu % should-expand? itemfn) grouped)]
    (into [:div.media-items.horiz-container] results)))

(defn random-search-prompt []
  (let [prompts ["CE DORITI?"
                 "CU CE VA PUTEM SERVI?"
                 "SRCH:"
                 "CAUT:"
                 "SI VREI?"]
        index (rand-int (count prompts))]
    (nth prompts index)))


(defn media-items-component [items opts]
    (let [{:keys [group-by search-string expand-all? item-view-mode]} opts
          group-by (if (str/blank? search-string) group-by :plain)
          itemfn (if (= item-view-mode :plain)
                   item->li
                   media-item-detail/item->detail-item)]
      (cond
        (= group-by :tag) (render-by-tags expand-all? itemfn)
        (= group-by :plain) (render-by-letter search-string expand-all? itemfn))))

(defn media-page [opts]
  (let [opts *display-options*
        search-prompt (random-search-prompt)]
    (fn []
      [crt-page
       [:div.media-page
        [:h4.page-title "PLANETA MOLDOVA"]
        [search-component search-prompt opts]
        [:div.v16px]
        [:div.page-content
         (let [items (:media ALLMEDIA)]
           [media-items-component items @opts])]]])))




