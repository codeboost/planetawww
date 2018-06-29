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
   [plawww.navbar.core :as navbar]
   [plawww.medialist.toolbar :as toolbar]
   [plawww.mediadb.core :as media-db]
   [plawww.utils :as utils]
   [reagent.core :as r]
   [reagent.session :as session]))

(defonce *state* (r/atom {:group-by         :tag
                          :item-view-mode   :plain
                          :expanded-letters #{}
                          :expanded-tags    #{}
                          :show-all?        false
                          :detail-items?    false
                          :selected-id ""}))

(defn set-opts [opts]
  (js/console.log "media-page/set-opts: " opts)
  (swap! *state* merge opts))

(defn- detail-title [{:keys [title duration]}]
  (str title " - " (utils/format-duration duration)))

(defn item->li [{:keys [title id description] :as item} {:keys [detail-items? selected-id]}]
  "Menu item to hiccup."
  (let [selected? (= id selected-id)
        title     (if detail-items? (detail-title item) title)]
    ^{:key id}
    [:li.media-item
     [:a {:href  (str "/media/" id)
          :class (when selected? :selected)}
      title]
     (when detail-items?
       [:div.description [:i (when description
                               (-> description
                                   (str/replace "<p>" "")   ;Rudimentary and temporary
                                   (str/replace "</p>" "")))]])]))


(defn toggle-expanded-tag [title]
  (swap! *state* update-in [:expanded-tags] utils/toggle-item title))

(defn toggle-expanded-letter [letter]
  (swap! *state* update-in [:expanded-letters] utils/toggle-item letter))

(defn menu->hiccup [{:keys [title items]} {:keys [expanded? detail-items?]}]
  "Renders a menu and its items.
  A 'menu' in this context is a div which displays a title and optionally a `ul` containing  child items."
  (let [disp-title (if detail-items?
                       (str title " - " (count items))
                       title)]
    [:div.menu
     [:div.title
      [:a {:href     "#"
           :on-click (fn [event]
                       (toggle-expanded-tag title)
                       (.preventDefault event))

           :class    (if expanded? "opened" "")}
       disp-title]
      (when (and expanded? (pos? (count items)))
        [:ul.items items])]]))

(defn tag-component
  ""
  [{:keys [title items]} opts]
  (let [menu {:title (str/upper-case title)
              :items (map #(item->li % opts) items)}]
    [menu->hiccup menu opts]))

(defn- expand? [title selected-tags]
  (let [clean-str      (comp str/trim str/lower-case)
        title          (clean-str title)
        selected-tags (if (string? selected-tags)
                        #{(clean-str selected-tags)}
                        (set (map clean-str selected-tags)))]
    (some? (some #{title} selected-tags))))

(defn render-tag-components
  "Given a list of items grouped by tags, render the tag titles and, if expanded, the items."
  [tagged {:keys [expanded-tags show-all?] :as opts}]
  (map (fn [{:keys [title] :as item}]
         [tag-component item (assoc opts :expanded? (or show-all? (expand? title expanded-tags)))])
       tagged))

(defonce by-tags* (memoize media-db/by-tags))

(defn items-by-tag
  "Collection of Tag components. Each Tag component has a bunch of items, which are hidden by default.
  Only items in `expanded-tags` Tags are visible.
  If `show-all?` then all items are visible."
  [media-items opts]
  (let [tagged (by-tags* media-items)
        comps  (render-tag-components tagged opts)]
    (into [:div.media-items.horiz-container] comps)))

;Todo: Search needs its own state atom
(defn render-search-results [items s expanded-tags no-results-fn]
  (let [items (media-db/search-in-items items s)
        tagged-items (media-db/filter-tagged (by-tags* items) items s)]
    [:div.search-results
     (when (seq tagged-items)
       [:div.tags
        (into [:div] (render-tag-components tagged-items {:expanded-tags expanded-tags
                                                          :show-all? false
                                                          :detail-items? false}))
        [:div "---"]])
     [:ul.items
      (if (seq items)
        (doall (map item->li items))
        [:li.no-results no-results-fn])]]))

(defn- group-by-first-letter [items]
  (group-by #(utils/extract-first-letter (:title %)) items))

(def group-by-first-letter* (memoize group-by-first-letter))

(defn letter-component [letter]
  [:div.letter
   [:a {:href :#
        :on-click (fn [e]
                    (toggle-expanded-letter letter)
                    (.preventDefault e))}
    letter]])

(defn expanded-items-component [items]
  [:ul.items (doall (map item->li items))])

(defn- items-by-alphabet
  "Produces a collection of components with items grouped by first letter.
  `items` - items to display.
  `expanded` - a set of letters. The items in these letter are displayed, otherwise only the letter is visible.
  `show-all` - all items are displayed."
  [items {:keys [expanded-letters show-all?]}]
  ;TODO: scroll to *letter
  (let [items-by-first-letter (group-by-first-letter* items)
        first-letters         (sort (keys items-by-first-letter))]
    (into [:ul.all-letters]
      (map
       (fn [letter]
         (let [show-items? (or show-all? (expanded-letters letter))]
           ^{:key (str "L_" letter)}
           [:li
            [letter-component letter]
            (when show-items?
              {:class :expanded}
              [expanded-items-component (get items-by-first-letter letter)])]))
       first-letters))))

(defn media-items-component [items state]
  (let [{:keys [group-by]} state]
    (case group-by
      :tag [items-by-tag items state]
      :plain [items-by-alphabet items state])))

(defn media-page [media-items]
  (fn []
    [:div.media-page
     [:div.components
      [toolbar/buttons *state*]
      [:div.v16px]
      [:div.page-content
       [media-items-component media-items @*state*]]]]))


(comment
 (def media-items (session/get :media-items))
 (sort (keys (group-by-first-letter* media-items)))

 (media-items-component media-items @*state*)

 (-> media-items
     media-page)

 (-> media-items
     (search-in-tags "musi")
     (render-list-of-tags)
     (seq)))