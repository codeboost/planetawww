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
                          :expanded-letters #{}
                          :expanded-tags  #{}
                          :show-all?      false
                          :detail-items?  false}))

(defn set-opts [opts]
  (println "media-page/set-opts: " opts)
  (swap! *state* merge opts))

(defn- detail-title [{:keys [title duration]}]
  (str title " - " (utils/format-duration duration)))


(defn item->li [{:keys [title id description duration] :as item}]
  "Menu item to hiccup."
  (let [detail? (:detail-items? @*state*)
        selected? (= id (:selected-id @*state*))
        title (if detail? (detail-title item) title)]
    ^{:key id}
    [:li.media-item
     [:a {:href (str "/media/" id)
          :class (when selected? :selected)}
      title]
     (when detail?
        [:div.description (when description
                            (-> description
                              (str/replace "<p>" "") ;Rudimentary and temporary
                              (str/replace "</p>" "")))])]))


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

(defn toggle-item
  "Toggles an item in a collection.
   Returns a set containing the items in coll with `text` added if it wasn't in the collection or removed if it was."
  [coll text]
  (set
   (if ((set coll) text)
     (remove #{text} coll)
     (conj coll text))))

(defn toggle-expanded-tag [title]
  (swap! *state* update-in [:expanded-tags] toggle-item title))

(defn toggle-expanded-letter [letter]
  (swap! *state* update-in [:expanded-letters] toggle-item letter))

(defn menu->hiccup [{:keys [title items] :as item} expanded?]
  "Renders a menu and its items.
  A 'menu' in this context is a div which displays a title and optionally a `ul` containing  child items."
  (let [title (if (:detail-items? @*state*)
                (str title " - " (count items))
                title)]
    [:div.menu
     [:div.title
      [:a {:href "#"
           :on-click (fn [event]
                      (toggle-expanded-tag title)
                      (.preventDefault event)
                      false)
           :class (if expanded? "opened" "")}
       title]
      (when (and expanded? (pos? (count items)))
        [:ul.items items])]]))

(defn tag-component
  ""
  [{:keys [title items]} expanded?]
  (let [menu {:title (str/upper-case title)
              :items (map item->li items)}]
    [menu->hiccup menu expanded?]))

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
  (println "group-by-tag called")
  (let [tags (unique-tags* media-items)]
    (mapv #(text->tag media-items %) tags)))

(defn by-tags [media-items]
  "Returns a list of Tag structures, each with following keys: [:title :items :num-items]"
  (sort-by
    :title
    (group-by-tag media-items)))

(defn- expand? [title selected-tags]
  (let [clean-fn (comp str/trim str/lower-case)
        title (clean-fn title)
        selected-tags (if (string? selected-tags)
                        #{(clean-fn selected-tags)}
                        (set (map clean-fn selected-tags)))]
    (some? (some #{title} selected-tags))))

(defonce by-tags* (memoize by-tags))
(defonce items-for-tag* (memoize items-for-tag))

(defn render-tag-components
  "Given a list of items grouped by tags, render the tag titles and, if expanded, the items."
  [tagged expanded-tags show-all?]
  (map (fn [{:keys [title] :as item}]
         [tag-component item (or show-all? (expand? title expanded-tags))])
       tagged))

(defn render-by-tag [media-items expanded-tags show-all?]
  (let [tagged (by-tags* media-items)
        comps (render-tag-components tagged expanded-tags show-all?)]
    (into [:div.media-items.horiz-container] comps)))

(defn search-in-items [media-items search-string]
  (filter (fn [{:keys [title]}]
            (search-match? title search-string)) media-items))

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

(defn- group-by-tag-starting-with
  "Returns a collection of items each with the keys [:title :items :num-items] grouped by tag title starting with `s`."
  [items s]
  (let [titles (tag-titles items s)] ;get unique tags matching s
    (filter #(titles (:title %)) (by-tags* items)))) ;filter only the matching


(defn render-search-results [items s expanded-tags]
  (let [item-results (search-in-items items s)
        tagged-items (group-by-tag-starting-with items s)]
    [:div.search-results
     (when (seq tagged-items)
       [:div.tags
        (into [:div] (render-tag-components tagged-items expanded-tags false))
        [:div "---"]])
     [:ul.items
      (if (seq item-results)
        (doall (map item->li item-results))
        [:li.no-results (search-component/random-not-found-msg (session/get :xx?))])]]))



(defn- letter-click-handler [letter]
  (fn [event]
    (toggle-expanded-letter letter)
    (.preventDefault event)
    false))

(defn- group-by-first-letter [items]
  (println "group-by-first-letter called.")
  (group-by #(utils/extract-first-letter (:title %)) items))

(def group-by-first-letter* (memoize group-by-first-letter))

(defn letter-component [letter]
  [:div.letter
   [:a
    {:href :#
     :on-click (letter-click-handler letter)}
    letter]])

(defn expanded-items-component [items]
  [:ul.items (doall (map item->li items))])

(defn- render-by-letter
  "expanded is an atom"
  [expanded items show-all?]
  ;TODO: scroll to *letter
  (let [items-by-first-letter (group-by-first-letter* items)
        first-letters (sort (keys items-by-first-letter))]
    (into [:ul.all-letters]
      (map
       (fn [letter]
         (let [show-items? (or show-all? (@expanded letter))]
           ^{:key (str "L_" letter)}
           [:li
            [letter-component letter]
            (when show-items?
              {:class :expanded}
              [expanded-items-component (get items-by-first-letter letter)])]))
       first-letters))))

(defn media-items-component [items opts]
  (let [{:keys [group-by search-string expanded-tags show-all?]} @opts
        searching? (not (str/blank? search-string))
        group-by (if searching? :plain group-by)
        expanded (r/cursor opts [:expanded-letters])]
    (if searching?
      (render-search-results items search-string expanded-tags)
      (case group-by
        :tag (render-by-tag items expanded-tags show-all?)
        :plain (render-by-letter expanded items show-all?)))))

(defn media-page [media-items]
  (fn []
    [:div.media-page
     [:div.components
      [search-component/search-component *state*]
      [:div.v16px]
      [:div.page-content
       [:div
        (let [items media-items]
         [media-items-component items *state*])]]]]))


(comment
 (def media-items (session/get :media-items))
 (sort (keys (group-by-first-letter* media-items)))

 (-> media-items
     (search-in-tags "musi")
     (render-list-of-tags)
     (seq)))