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
   [plawww.paths :refer [explorer-path]]
   [plawww.ui :as ui]
   [plawww.utils :refer [search-match?]]
   [reagent.core :as r]
   [goog.string :as gstring]
   [goog.string.format]
   [plawww.media-item.media-item :as media-item]))

(defonce *state* (r/atom {:sort-by :title
                          :included-tags #{}
                          :visible-dialog :none
                          :category nil}))

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
         {:href (explorer-path (str "tag/" tag))}
         tag-text]
        " "]))))

(defn m->detail [state {:keys [title id type tags publish_on description_plain] :as m}]
  [:div.item-detail
   [:img {:src (paths/l-image-path id)}]
   [:div.content
    [:h3 title]
    [:p description_plain]]])

(def show-anims [:show-scaled :show-scaled-x :show-scaled-y])

(defn random-animation-class []
  (let [anim (nth show-anims (rand-int (count show-anims)))]
    anim))

(defn m->item [i {:keys [title id type tags publish_on description_plain type] :as m} {:keys [anim-class category-name]}]
  ^{:key id}
  [:li.item {:class anim-class
             :style {:visibility :hidden
                     :animation-delay (str (* i 100) "ms")}}
   [:a {:href :#
        :on-click #(session/put! :current-media-item m)}
    [:span.item-container
     [:img.thumbnail {:src (paths/media-image-path id {:show-custom? (= type "video")
                                                       :category-name category-name
                                                       :size :thumbnail})}]
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

(defn tags-component [included-tags]
  [:div.taglist
   [:div.compressor]
   [:div {:on-click #(set-opts {:visible-dialog :tag-editor})}
    (if (empty? included-tags)
      "Toate"
      (clojure.string/join ", " included-tags))]
   [:div.compressor]])

(defn tag-editor [_ _ _]
  (let [filter-options (r/atom {:search-string ""})]
    (fn [all-tags included-tags {:keys [tag-click all-click close-click]}]
      (let [ss (:search-string @filter-options)
            filtered-tags (if-not (empty? ss)
                            (filter #(search-match? % ss) all-tags)
                            all-tags)]
        [:div.tag-editor
         [:div.tags-toolbar
          [:div.buttons
           [:div.all-tags
            {:on-click (fn []
                         (swap! filter-options assoc :search-string "")
                         (all-click))
             :class (when (empty? included-tags) :selected)} "TOATE"]
           [:div.gata {:on-click close-click}
            "GATA"]]
          [:div.filtering
           [:input.search-box {:type      "text"
                               :on-change #(swap! filter-options assoc :search-string (-> % .-target .-value))
                               :value (:search-string @filter-options)}]]]
         [:div.tags-container
          (into [:ul.tags]
            (map (fn [s]
                   (let [class-name (when (included-tags s) :selected)]
                     [:li.tag
                      {:class class-name
                       :on-click #(tag-click s)} s])) filtered-tags))]]))))

(defn- tag-editor-modal [state {:keys [included-tags all-tags]}]
  [ui/modal
   {:on-close #(swap! state assoc :visible-dialog :none)
    :visible? true}
   [:div
    [tag-editor
     all-tags
     included-tags
     {:tag-click #(swap! state update :included-tags (if (included-tags %) disj conj) %)
      :all-click #(swap! state assoc :included-tags #{})
      :close-click #(swap! state assoc :visible-dialog :none)}]]])

(defn- media-info-modal [state current-item]
  (let [on-close (fn []
                   (session/put! :current-media-item nil)
                   (swap! state assoc :visible-dialog :none))]
    [ui/modal
     {:on-close on-close
      :visible? true}
     [media-item/item-info-component
      {:on-play #(accountant.core/navigate! (explorer-path (:id current-item)))
       :on-close on-close}
      {:selected-item current-item}]]))

(defn explorer-page []
  (let [state *state*
        media-items (session/get :media-items)
        sort-by-cursor (r/cursor state [:sort-by])
        current-item (session/cursor [:current-media-item])]
    (fn []

      (let [included-tags (:included-tags @state)
            media-items (db/items-for-category media-items (get-in @state [:category :id]))
            all-tags (db/unique-tags* media-items)
            media-items (db/items-for-tags media-items included-tags)
            sort-fn (sorter @sort-by-cursor)
            media-items (sort-fn media-items)
            visible-dialog (or (and @current-item :media-info) (:visible-dialog @state))
            searching? (not (empty? (:search-string @state)))
            anim-class :show-scaled-y]
        [:div.explorer
         (when-not searching?
           [toolbar/explorer-buttons {:sort-by (:sort-by @state)
                                      :clicked #(swap! state assoc :sort-by %)}])
         [:span.spacer]
         (when (:category @state)
           [plawww.categories.categories/category-component (:category @state) {:url (paths/categories-path "")
                                                                                :index 0
                                                                                :scale-on-hover? false}])
         [:span.spacer]
         [tags-component (:included-tags @state)]
         [:span.spacer]
         (into
          [:ul.items]
          (map-indexed #(m->item %1 %2 {:anim-class anim-class
                                        :category-name (db/any-category-slug %2)}) media-items))
         [:span.spacer]
         (case visible-dialog
           :tag-editor
           [tag-editor-modal state {:included-tags included-tags
                                    :all-tags all-tags}]
           :media-info
           [media-info-modal state @current-item]
           nil)]))))

