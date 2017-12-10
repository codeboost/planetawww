;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.medialist
  (:require
   [cljsjs.typedjs]
   [cljs.test :refer-macros [deftest is testing run-tests]]
   [clojure.string :as str]
   [plawww.medialist.alphabet :as alphabet]
   [plawww.media-item-detail :as media-item-detail]
   [plawww.search-component :refer [search-component]]
   [plawww.utils :as utils]
   [reagent.core :as r]
   [reagent.session :as session]))

(def default-options {:group-by       :tag
                      :item-view-mode :plain
                      :search-string  ""
                      :cur-letter     ""
                      :tags           #{}})

(defonce *display-options* (r/atom default-options))

(defn item->li [{:keys [title id]}]
  "Menu item to hiccup."
  (let [href (str "/media/" id)]
    ^{:key id} [:li [:a {:href href} title]]))

(defn search-match? [title search-string]
  (or (str/blank? search-string)
      (str/starts-with?
        (str/lower-case title)
        (str/lower-case search-string))))

(defn toggle-atom-handler
  "Returns click handler, which, when called, toggles the atom's value (using `not`).
  Presumably, the atom contains a `boolean` value."
  [*a]
  (fn [e]
    (.preventDefault e)
    (reset! *a (not @*a))))

(defn menu->hiccup [{:keys [title items] :as menu} *expanded?]
  "Renders a menu and its items.
  A 'menu' in this context is a div which displays a title and optionally a `ul` containing  child items."
  [:div.menu
   [:div.title
    [:a {:on-click (toggle-atom-handler *expanded?)
         :class    (if @*expanded? "opened" "")} title]]
   (when (and @*expanded? (pos? (count items)))
    [:ul.items items])])

(defn tag-component
  ""
  [{:keys [title items num-items]} expanded?]
  (let [expanded? (r/atom expanded?)
        menu {:title (str/upper-case title)
              :items (map item->li items)}]
    [menu->hiccup menu expanded?]))

(defn tags-from-items
  "Returns a set of unique tags extracted from the media items."
  [media-items]
  (->> media-items
       (map :tags)
       (apply concat)
       (map str/trim)
       (set)))

(defn items-for-tag [media-items tag]
  "Returns items from media-items which contain the tag `tag`."
  (filter (fn [{:keys [tags]}]
            (some #(= tag %) tags)) media-items))

(defn- text->tag
  "Make a tag struct from a tag title.
  The returned tag structure contains the following keys:
    :title     - title of the tag.
    :items     - a vector where each element is a media item from `media-items` which is tagged with `tag-title`.
    :num-items - number of elements in the items vector, for slightly faster sorts"
  [media-items tag-title]
  (let [items (items-for-tag media-items tag-title)
        num-items (count items)]
    {:title     tag-title
     :items     items
     :num-items num-items}))

(defn- group-by-tag
  "Groups media items from the `media-items` vector by tag.
  Returns a collection of tag structures."
  [media-items]
  (let [tag-titles (tags-from-items media-items)
        tags (mapv #(text->tag media-items %) tag-titles)]
    tags))

(defn by-tags [media-items]
  "Returns a list of maps with following keys: [:tag :items :num-items]"
  (reverse
    (sort-by
      :num-items
      (group-by-tag media-items))))

(defonce padding-menus (vec (repeat 16 [:div.menu [:div.title]])))

(defn render-by-tags [media-items]
  (let [by-tags* (memoize by-tags)
        tagged (by-tags* media-items)
        menus (mapv #(tag-component % false) tagged)
        menus (into menus padding-menus)]
    (into [:div.media-items.horiz-container] menus)))

(defn search-in-items [media-items search-string]
  (filter (fn [{:keys [title]}]
            (search-match? title search-string)) media-items))

(defn render-items [items itemfn]
  (into [:ul.items] (mapv itemfn items)))

(defn starting-with
  "Returns the items who's `:title` starts with `letter`."
  [items letter]
  (filter #(utils/starts-with-letter? (:title %) letter) items))

(defn render-alphabet [*letter items]
  [alphabet/alphabet-component *letter (utils/first-letters items)])

(defn render-by-letter [*letter items]
  [:div.by-letters
   (render-alphabet *letter items)
   [render-items (starting-with items @*letter) item->li]])

(defn render-search-results [search-string items]
  (let [results (search-in-items items search-string)]
    [render-items results item->li]))

(defn media-items-component [items opts]
    (let [{:keys [group-by search-string item-view-mode]} @opts
          searching? (not (str/blank? search-string))
          group-by (if searching? :plain group-by)
          *letter (r/cursor opts [:cur-letter])]
      (cond
        (= group-by :tag) (render-by-tags items)
        (= group-by :plain)
        (if searching? (render-search-results search-string items)
                       (render-by-letter *letter items)))))

(defn medialist [opts media-items]
  (let [opts *display-options*]
    (fn []
       [:div.media-page
        [:h4.page-title "PLANETA MOLDOVA"]
        [search-component opts]
        [:div.v16px]
        [:div.page-content
         (let [items media-items]
           [media-items-component items opts])]])))

(comment

  (def ALLMEDIA (session/get! :allmedia))

  (render-by-tags ALLMEDIA false item->li)

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