;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.medialist.explorer
  (:require
   [plawww.paths :as paths]
   [reagent.session :as session]
   [plawww.medialist.toolbar :as toolbar]
   [plawww.components.components :refer [minimise-button]]
   [plawww.mediadb.core :as db]
   [plawww.ui :as ui]
   [reagent.core :as r]
   [goog.string :as gstring]
   [goog.string.format]
   [plawww.media-item.media-item :as media-item]))

(defonce *state* (r/atom {:sort-by :title
                          :included-tags #{}
                          :visible-dialog :none}))

(defn set-opts [opts]
  (swap! *state* merge opts))

(defn format-date [d]
  (gstring/format "%d-%02d-%02d" (.getFullYear d) (.getMonth d) (.getDay d)))

(defn tag-list-comp [tags]
  (into
   [:ul.tags]
   (for [tag tags]
     (let [tag-text (if (= tag (last tags)) tag (str tag ","))]
       [:li
        [:a
         {:href (str "/explorer/tag/" tag)}
         tag-text]
        " "]))))

(defn m->detail [state {:keys [title id type tags publish_on description_plain] :as m}]
  [:div.item-detail
   [:img {:src (paths/l-image-path id)}]
   [:div.content
    [:h3 title]
    [:p description_plain]]])

(defn m->item [{:keys [title id type tags publish_on description_plain] :as m}]
  ^{:key id}
  [:li.item
   [:a {:href (str "/explorer/" id)}
    [:span.item-container
     [:img.thumbnail {:src (paths/s-image-path id)}]
     [:span.item-info
      [:div.title title]
      [:div.description description_plain]
      #_[tag-list-comp tags]]
     [:span.item-more-info
      [:span.published (format-date publish_on)]]]]])

(defn sorter
  "Returns a function, which when called with a collection of media items,
  will sort the collection by the key requested in `sk`:
    :title - sorts items by :title key (string)
    :new   - sorts items by :publish_on (date)
    :old   - sorts items by :pulish_on, descending (date)."
  [sk]
  (case sk
    :title (partial sort-by :title)
    :new   (partial sort-by :publish_on #(compare %2 %1))
    :old   (partial sort-by :publish_on #(compare %1 %2))
    (partial sort-by :title)))

(defn parse-dates [media-items]
  (map #(update-in % [:publish_on] (fn [s]
                                     (when s
                                       (js/Date. s)))) media-items))

(defn tags-component [included-tags]
  [:div.taglist
   [:div {:on-click #(set-opts {:visible-dialog :tag-editor})}
    (if (empty? included-tags)
      "Toate"
      (clojure.string/join ", " included-tags))]])

(defn tag-editor-component [all-tags included-tags {:keys [tag-click all-click close-click]}]
  (let [keydown-handler (fn [e]
                          (when (#{"Escape" "Esc"} (.-key e))
                            (close-click)))]
    (r/create-class
     {:component-did-mount (fn []
                             (.addEventListener js/window "keydown" keydown-handler))
      :component-will-unmount (fn []
                                (.removeEventListener js/window "keydown" keydown-handler))
      :reagent-render
      (fn [all-tags included-tags _]
        [:div.tag-editor
         #_[minimise-button "x" close-click]
         [:div.buttons
          [:div.all-tags
           {:on-click all-click
            :class (when (empty? included-tags) :selected)} "TOATE"]
          [:div.gata {:on-click close-click}
           "GATA"]]
         [:div.tags-container
          (into [:ul.tags]
                (map (fn [s]
                       (let [class-name (when (included-tags s) :selected)]
                         [:li.tag
                          {:class class-name
                           :on-click #(tag-click s)} s])) all-tags))]])})))

(defn explorer-page []
  (let [state *state*
        media-items (session/get :media-items)
        all-tags (db/unique-tags* media-items)
        sort-by-cursor (r/cursor state [:sort-by])
        current-item (session/cursor [:current-media-item])]
    (fn []
      (let [included-tags (:included-tags @state)
            media-items (db/items-for-tags media-items included-tags)
            sort-fn (sorter @sort-by-cursor)
            media-items (sort-fn media-items)
            visible-dialog (or (and @current-item :media-info) (:visible-dialog @state))]

        [:div
         [:div.explorer
          [toolbar/explorer-buttons state]
          [:span.spacer]
          [tags-component (:included-tags @state)]
          [:span.spacer]
          (into
           [:ul.items]
           (map m->item media-items))
          [:span.spacer]
          (case visible-dialog
            :tag-editor
            [ui/modal
             {:on-close #(swap! state assoc :visible-dialog :none)
              :visible? true}
             [:div
              [tag-editor-component
               all-tags
               included-tags
               {:tag-click #(swap! state update :included-tags (if (included-tags %) disj conj) %)
                :all-click #(swap! state assoc :included-tags #{})
                :close-click #(swap! state assoc :visible-dialog :none)}]]]
            :media-info
            [:div [media-item/item-info-component
                   {:on-play (fn []
                               (plawww.media-player.core/set-current-item @current-item)
                               (session/put! :current-media-item nil))}
                   {:selected-item @current-item}]]
            nil)]]))))

