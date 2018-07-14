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
                          :show-all?        false
                          :detail-items?    false
                          :selected-id ""}))

(defn set-opts [opts]
  (js/console.log "media-page/set-opts: " opts)
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
     [:a {:href  (str "/media/" id)
          :class (when selected? :selected)}
      title]
     (when detail-items?
       [:div.description [:i (when description
                               (-> description
                                   (str/replace "<p>" "")   ;Rudimentary and temporary
                                   (str/replace "</p>" "")))]])]))

(defn toggle-expanded-letter [letter]
  (swap! *state* update-in [:expanded-letters] utils/toggle-item letter))

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
  Clicking on a tag-component's title will toggle the visibility of child items (expanded tags)."
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


(defn render-one-tag
  "This renders just one tag component, which is expanded and all its items are visible."
  [items tag-title state]
  (let [tag (->> (media-db/by-tags items)
                 (filter #(= tag-title (:title %)))
                 (first))]
    [tag-component tag
     (assoc state :expanded? true)]))


(defn items-by-tag
  "Renders a collection of Tag components or just one single Tag.
  included-tags
  Each Tag component has a bunch of items, which are hidden by default.
  If `show-all?` then all items are visible."
  [items {:keys [included-tags] :as opts}]
  (let [tagged (by-tags* items)
        tagged (if-not
                (pos? (count included-tags))
                tagged
                (filter (fn [{:keys [title]}] (included-tags title))
                        tagged))]
    [:div.media-items.horiz-container
     [items-by-tag-component tagged opts]]))

(by-tags*
 (media-db/items-for-tags (session/get :media-items) #{"music"}))

;Todo: Search needs its own state atom
(defn render-search-results [items s no-results-fn]
  (let [items (media-db/search-in-items items s)
        tagged-items (media-db/filter-tagged (by-tags* items) items s)]
    [:div.search-results
     (when (seq tagged-items)
       [:div.tags
        [items-by-tag-component tagged-items {:show-all?      false
                                              :detail-items?  false}]
        [:div "---"]])
     [:ul.items
      (if (seq items)
        (doall (map render-menu-item items))
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
  [:ul.items (doall (map render-menu-item items))])

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
  (let [{:keys [group-by included-tags]} state
        items (media-db/items-for-tags items included-tags)]
    (case group-by
      :tag [items-by-tag items state]
      :plain [items-by-alphabet items state])))

(defn media-page [media-items]
  [:div.media-page
   {:tab-index 0
    :on-key-press (fn [event]
                    (if (= "d" (str/lower-case (.-key event)))
                      (swap! *state* update :detail-items? not)))}
   [:div.components
    [toolbar/buttons *state*]
    [:div.v16px]
    [:div.page-content
     [media-items-component media-items @*state*]]]])



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