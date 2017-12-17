;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.medialist.core
  (:require
   [cljsjs.typedjs]
   [clojure.string :as str]
   [plawww.medialist.alphabet :as alphabet]
   [plawww.media-item-detail :as media-item-detail]
   [plawww.medialist.search-component :refer [search-component]]
   [plawww.utils :as utils]
   [reagent.core :as r]
   [reagent.session :as session]))

(defonce *state* (r/atom {:group-by       :tag
                          :item-view-mode :plain
                          :search-string  ""
                          :cur-letter     ""
                          :cur-tag        ""
                          :show-all?      false
                          :tags           #{}}))


(defn set-opts [opts]
  (println "media-page/set-opts: " opts)
  (swap! *state* merge opts))

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

(defn menu->hiccup [{:keys [title items] :as menu} expanded?]
  "Renders a menu and its items.
  A 'menu' in this context is a div which displays a title and optionally a `ul` containing  child items."
  [:div.menu
   [:div.title
    [:a {:href  (str "/media/tag/" title)
         :class (if expanded? "opened" "")}
     title]
    (when (and expanded? (pos? (count items)))
      [:ul.items items])]])

(defn tag-component
  ""
  [{:keys [title items]} expanded?]
  (let [menu {:title (str/upper-case title)
              :items (map item->li items)}]
    [menu->hiccup menu expanded?]))

(defn tags-from-items
  "Returns a set of unique tags extracted from the media items."
  [media-items]
  (->> media-items
       (map :tags)
       (apply concat)
       (map str/trim)
       (remove empty?)
       (set)))

(defn equal-tag? [tag1 tag2]
  (= (str/trim (str/lower-case tag1))
     (str/trim (str/lower-case tag2))))

(defn items-for-tag [media-items tag]
  "Returns items from media-items which contain the tag `tag`."
  (filter (fn [{:keys [tags]}]
            (some #(equal-tag? tag %) tags)) media-items))

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

(defn- expand? [{title :title} selected-tag]
  (= (str/trim (str/lower-case title))
     (str/trim (str/lower-case selected-tag))))

(defn render-tags-and-items [media-items selected-tag]
  (let [by-tags* (memoize by-tags)
        tagged (by-tags* media-items)
        menus (mapv #(tag-component % true) tagged)
        menus (into menus padding-menus)]
    (into [:div.media-items.horiz-container] menus)))


(defn render-list-of-tags [media-items]
  (into [:div.media-items.horiz-container]
    (->> media-items
     (tags-from-items)
     (filter (comp pos? count))
     (map #(text->tag [] %))
     (map #(tag-component % false)))))


(defn render-one-tag [media-items selected-tag]
  (let [items (items-for-tag media-items selected-tag)]
    [:div.media-items.horiz-container
     (tag-component {:title selected-tag
                     :items items
                     :num-items (count items)} true)]))

(defn render-by-tag
  "Renders media items grouped by tags.
  If show-all? is false
    If `selected-tag` is an empty string, the tag list is rendered.
    If `selected-tag` is non-empty, the items tagged with `selected-tag` are rendered.
  else, if show-all? is true, all tags and their items are displayed. "
  [media-items selected-tag show-all?]
  (if show-all?
    (render-tags-and-items media-items selected-tag)
    (if (pos? (count selected-tag))
      (render-one-tag media-items selected-tag)
      (render-list-of-tags media-items))))

(comment
  (->>
    (session/get :media-items)
    (tags-from-items)))

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
  (let [{:keys [group-by search-string cur-tag show-all?]} @opts
        searching? (not (str/blank? search-string))
        group-by (if searching? :plain group-by)
        *letter (r/cursor opts [:cur-letter])]
    (cond
      (= group-by :tag)
      (render-by-tag items cur-tag show-all?)
      (= group-by :plain)
      (if searching? (render-search-results search-string items)
                     (render-by-letter *letter items)))))

(defn media-page [media-items]
  (fn []
    [:div.media-page
     [:h4.page-title "PLANETA MOLDOVA"]
     [search-component *state*]
     [:div.v16px]
     [:div.page-content
      (let [items media-items]
        [media-items-component items *state*])]]))

