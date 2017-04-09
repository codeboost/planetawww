;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.medialist
  (:require [clojure.string :as str]
            [cljsjs.typedjs]
            [reagent.core :as r]
            [cljs.test :refer-macros [deftest is testing run-tests]]
            [plawww.media-item-detail :as media-item-detail]
            [plawww.search-component :refer [search-component]]
            [plawww.text-menu-component :refer [menu->hiccup]]))


(defonce ALLMEDIA (js->clj js/kolbasulPlanetar :keywordize-keys true))

(def default-options {:group-by      :tag
                      :item-view-mode :plain
                      :dirty         true
                      :search-string ""
                      :cur-letter ""
                      :expand-all?   false
                      :tags          #{}})

(defonce *display-options* (r/atom default-options))

(defn item->li [{:keys [title id]}]
  "Menu item to hiccup."
  (let [href (str "/media/" id)]
    ^{:key id} [:li [:a {:href href} title]]))

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

(defn render-by-tags [expand-all? itemfn]
  (let [tagged BYTAG
        menus (map #(group->menu % expand-all? itemfn) tagged)]
    (into [:div.media-items.horiz-container] menus)))

;-----------------------------

(defn search-in-items [media-items search-string]
  (filter (fn [{:keys [title]}]
            (search-match? title search-string)) media-items))

(defn extract-first-letter [str]
  (let [letter (or (first (str/trim str)) "#")
        letter (str/upper-case letter)
        letter (if (re-matches #"[0-9]+" letter)
                 "#"
                 letter)]
    letter))

(defn starts-with-letter? [word letter]
  (cond
    (= "#" letter) (some? (re-matches #"[0-9]+" (first (str/trim word))))
    :else (str/starts-with? (str/lower-case word) (str/lower-case letter))))

(defn first-letters [items]
  (apply sorted-set (map #(extract-first-letter (:title %)) items)))


(defn alphabet-component [letters selected on-click]
  (into [:ul]
        (for [letter letters]
          ^{:key letter}
          [:li [:a {:on-click (fn [e]
                                (.preventDefault e)
                                (on-click letter))
                    :class (if (starts-with-letter? letter selected) "selected" "")} letter]])))


(defn render-items [items itemfn]
  (into [:ul.items] (map itemfn items)))

(defn starting-with [items first-letter]
  (let [filtered
        (filter
          (fn [{:keys [title]}]
            (starts-with-letter? title first-letter)) items)]
    filtered))


(defn render-alphabet [acur items]
  [:div.alphabet
   [alphabet-component
    (first-letters items)
    @acur
    #(reset! acur %)]])

(defn render-by-letter2 [acur items itemfn]
  [:div.by-letters
   (render-alphabet acur items)
   [render-items (starting-with items @acur) itemfn]])

(defn render-search-results [search-string items itemfn]
  (let [results (search-in-items items search-string)]
    [render-items results itemfn]))

(defn media-items-component [items opts]
    (let [{:keys [group-by search-string expand-all? item-view-mode]} @opts
          searching? (not (str/blank? search-string))
          group-by (if searching? :plain group-by)
          itemfn (if (= item-view-mode :plain)
                   item->li
                   media-item-detail/item->detail-item)
          aletter (r/cursor opts [:cur-letter])]
      (cond
        (= group-by :tag) (render-by-tags expand-all? itemfn)
        (= group-by :plain)
        (if searching? (render-search-results search-string items itemfn)
                       (render-by-letter2 aletter items itemfn)))))

;(render-by-letter search-string expand-all? itemfn)


(defn media-page [opts]
  (let [opts *display-options*]
    (fn []
       [:div.media-page
        [:h4.page-title "PLANETA MOLDOVA"]
        [search-component opts]
        [:div.v16px]
        [:div.page-content
         (let [items (:media ALLMEDIA)]
           [media-items-component items opts])]])))
(comment
  (render-items (:media ALLMEDIA) "A" item->li)
  (deftest test-starts-with-first-letter?
    (is (= true (starts-with-letter? "a" "a")))
    (is (= true (starts-with-letter? "Alpha" "A")))
    (is (= true (starts-with-letter? "beta" "B")))
    (is (= false (starts-with-letter? "34" "B")))
    (is (= true (starts-with-letter? "34" "#"))))

  (deftest test-extract-first-letter
    (is (= "A" (extract-first-letter "Alpha")))
    (is (= "Z" (extract-first-letter "zebra")))
    (is (= "#" (extract-first-letter "10 Lions")))
    (is (= "#" (extract-first-letter "1984")))
    (is (= "#" (extract-first-letter ""))))

  (deftest test-first-letters
    (is
      (= #{"#" "A" "B" "G"}
         (first-letters [{:title "Alpha"}
                         {:title "Beta"}
                         {:title "Gamma"}
                         {:title "54"}]))))


  (run-tests))