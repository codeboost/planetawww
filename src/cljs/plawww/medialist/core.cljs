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
   [plawww.medialist.search-component :as search-component]
   [plawww.media-player.item-detail :as item-detail]
   [plawww.utils :as utils]
   [reagent.core :as r]
   [reagent.session :as session]))

(defonce *state* (r/atom {:group-by       :tag
                          :item-view-mode :plain
                          :search-string  ""
                          :cur-letter     "A"
                          :cur-tag        ""
                          :show-all?      false
                          :detail-items?  false}))


(defn set-opts [opts]
  (println "media-page/set-opts: " opts)
  (swap! *state* merge opts))


(defn item-plain [{:keys [title id]}]
  (let [href (str "/media/" id)
        selected? (= id (:selected-id @*state*))]
    ^{:key id} [:li.media-item
                [:a {:href href
                     :class (when selected? :selected)}
                 title]]))

(defn item->li [item]
  "Menu item to hiccup."
  (if (:detail-items? @*state*)
    (item-detail/item->detail-item item)
    (item-plain item)))

(defn search-match?
  "Returns true if `title` starts with `search-string`, regardless of case.
  If search string is empty, returns true."
  [title search-string]
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

(defn- expand? [{title :title} selected-tag]
  (= (str/trim (str/lower-case title))
     (str/trim (str/lower-case selected-tag))))

(defonce by-tags* (memoize by-tags))

(defn render-tags-and-items [media-items selected-tag]
  (let [tagged (by-tags* media-items)
        menus (mapv #(tag-component % true) tagged)]
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

(defn search-in-items [media-items search-string]
  (filter (fn [{:keys [title]}]
            (search-match? title search-string)) media-items))

(defn search-in-tags
  "Returns a collection of elements from `items` for which one of its tags matches `s`."
  [items s]
  (filter
   (fn [{:keys [tags]}]
     (some #(search-match? % s) tags))
   items))

(comment
 (def media-items (session/get :media-items))

 (-> media-items
   (search-in-tags "musi")
   (render-list-of-tags)
   (seq)))


(defn starting-with
  "Returns the items who's `:title` starts with `letter`."
  [items letter]
  (filter #(utils/starts-with-letter? (:title %) letter) items))

(defn render-search-results [search-string items]
  (let [item-results (search-in-items items search-string)
        tag-results (search-in-tags items search-string)]
    [:div.search-results
     (when (seq tag-results)
       [:div.tags
        (render-list-of-tags tag-results)
        [:hr]])
     [:ul.items
      (if (seq item-results)
        (doall (map item->li item-results))
        [:li.no-results (search-component/random-not-found-msg)])]]))

(def first-letters* (memoize utils/first-letters))

(defn render-alphabet [*letter items]
  [alphabet/alphabet-component *letter (first-letters* items)])

(defn- render-all-by-letters
  [*letter items]
  ;TODO: scroll to *letter
  (let [first-letters (first-letters* items)]
    (into [:ul.all-letters]
      (map
       (fn [the-letter]
         ^{:key (str "L_" the-letter)}
         [:li
          [:div.letter the-letter]
          [:ul.items (doall (map item->li (starting-with items the-letter)))]])
       first-letters))))


(defn render-by-letter [*letter items show-all?]
  (if show-all?
    (render-all-by-letters *letter items)
    [:div.by-letters
     (render-alphabet *letter items)
     [:ul.items (doall (map item->li (starting-with items @*letter)))]]))

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
                     (render-by-letter *letter items show-all?)))))

(defn media-page [media-items]
  (fn []
    [:div.media-page
     [search-component/search-component *state*]
     [:div.v16px]
     [:div.page-content
      [:div
       [:div.back-button [:a {:href "javascript:history.back();"} ".."]]
       (let [items media-items]
        [media-items-component items *state*])]]]))

