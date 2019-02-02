;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.medialist.search-results
  "This namespace has a long history. It is where the whole story started,
  I've been pounding on it for a long time, refactoring as I was learning
  more and more Clojure(script). You can check out the commit history.
  Eventually, most of the functionality has been reimplemented in `explorer`,
  while this namespace remains only for rendering search results.
  For historical reasons, I won't rename it, just a reminder of the long trip
  that I took."
  (:require
   [clojure.string :as str]
   [plawww.mediadb.core :as media-db]
   [plawww.paths :refer [explorer-path path-for-item-with-title]]
   [plawww.utils :as utils]
   [reagent.core :as r]))

(defonce *state* (r/atom {:show-all?        false
                          :detail-items?    false
                          :selected-id ""}))

(defn set-opts [opts]
  (swap! *state* merge opts))

(defn- mtype-str [type]
  (case type
    "audio" ""
    "video" "(V) "
    ""))

(defn- detail-title [{:keys [title duration type]}]
  (str (mtype-str type) title " - " (utils/format-duration duration)))

(defn render-menu-item [{:keys [title id description] :as item} {:keys [detail-items? selected-id]}]
  "Menu item."
  (let [selected? (= id selected-id)
        title     (if detail-items? (detail-title item) title)]
    ^{:key id}
    [:li.media-item
     [:a {:href  (path-for-item-with-title title)
          :class (when selected? :selected)}
      title]
     (when detail-items?
       [:div.description [:i (when description
                               (-> description
                                   (str/replace "<p>" "")   ;Rudimentary and temporary
                                   (str/replace "</p>" "")))]])]))

(defn render-menu [{:keys [title items]} {:keys [expanded? detail-items? on-title-clicked] :as opts}]
  "A 'menu' in this context is a div which displays a title and optionally a `ul` containing  child items.
  The name `menu` stuck around, so I'll keep it."
  (let [disp-title (if detail-items?
                       (str title " - " (count items))
                       title)]
    [:div.menu (if expanded? {:class :expanded})
     [:div.title
      [:a {:href     "#"
           :on-click (fn [event]
                       (on-title-clicked title)
                       (.preventDefault event))

           :class    (if expanded? "opened" "")}
       disp-title]
      (when (and expanded? (pos? (count items)))
        (into [:ul.items]
          (map #(render-menu-item % opts) items)))]]))

(defn tag-component
  "Renders a 'Tag'. Which is basically a container with a title and some child items.
  Expects `title` and `items` keys.
  opts can contain:
    :expanded?        - items are visible?
    :detail-items?    - details are enabled ?
    :on-title-clicked - handler for when the tag title is clicked.
    :selected-id      - id of the selected item"
  [{:keys [title items]} opts]
  (let [menu {:title (str/upper-case title)
              :items items}]
    [render-menu menu opts]))

(defn- expand? [title selected-tags]
  (let [clean-str      (comp str/trim str/lower-case)
        title          (clean-str title)
        selected-tags (if (string? selected-tags)
                        #{(clean-str selected-tags)}
                        (set (map clean-str selected-tags)))]
    (some? (some #{title} selected-tags))))


(defonce by-tags* (memoize media-db/by-tags))

(defn items-by-tag-component
  "Given a list of items, group by tags and render a bunch of `tag-component`s.
  Clicking on a tag-component's title will toggle the visibility of child items (expanded tags).
  If `show-all?` then all tags are considered expanded."
  [_ opts]
  (let [state (r/atom {:expanded-tags (:included-tags opts)})]
    (fn [tagged {:keys [show-all?] :as opts}]
      (let []
        (into [:div]
          (map (fn [{:keys [title] :as item}]
                 [tag-component item
                  (assoc opts :expanded? (or show-all? (expand? title (:expanded-tags @state)))
                              :on-title-clicked #(swap! state update-in [:expanded-tags] utils/toggle-item %))])
               tagged))))))

;Todo: Search needs its own state atom
(defn render-search-results [all-items s no-results-fn on-close]
  (let [key-handler #(when (= (.-keyCode %) 27) (on-close))]
    (r/create-class
     {:display-name "search-results"
      :component-did-mount #(.addEventListener js/window "keydown" key-handler)
      :component-will-unmount #(.removeEventListener js/window "keydown" key-handler)
      :reagent-render
      (fn [all-items s no-results-fn on-close]
        (let [items (media-db/search-in-items all-items s)
              tagged-items (media-db/by-tags-which-start-with (by-tags* all-items) s)]
          [:div.search-results
           [:div.close-button {:on-click on-close} "x"]
           [:div.search-results-content
            (when (seq tagged-items)
              [:div.tags
               [items-by-tag-component tagged-items {:show-all?      false
                                                     :detail-items?  false}]
               [:div "---"]])
            [:ul.items
             (if (seq items)
               (doall (map render-menu-item items))
               [:li.no-results no-results-fn])]]]))})))
