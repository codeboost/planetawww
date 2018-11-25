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
   [plawww.media-item.media-item :as media-item]
   [plawww.utils :as utils]))

(defonce *state* (r/atom {:sort-by :title
                          :included-tags #{}
                          :visible-dialog :none
                          :category nil
                          :detail? false}))

(defn set-opts [opts]
  (swap! *state* merge opts))

(defn format-date [d]
  (gstring/format "%d-%02d-%02d" (.getFullYear d) (.getMonth d) (.getDay d)))

(defn inline-tags-component [tags]
  (into
   [:ul.inline-tags]
   (for [tag tags]
     (let [tag-text (if (= tag (last tags)) tag (str tag ", "))]
       [:li tag-text]))))

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

(defn m->item [i {:keys [title id tags publish_on description_plain type duration] :as m} {:keys [anim-class category-name detail?]}]
  ^{:key id}
  [:li.item
   [:a {:href :#
        :on-click #(do
                     (session/put! :current-media-item m)
                     (plawww.media-player.core/set-detail-visible false))}
    [:span.item-container
     [:div.primary-info
      [:img.thumbnail {:src (paths/media-image-path id {:show-custom? (= type "video")
                                                        :category-name category-name
                                                        :size :thumbnail})}]
      [:span.item-info
       [:div.title title]
       [:div.description [:div.description-text description_plain]]]]
     (when detail?
       [:div.detail-info
        [:div.det.type type]
        [:div.det.duration (utils/format-duration duration "%sm %ss")]
        [:div.det.tags [inline-tags-component tags]]
        [:div.det.publish-on (format-date publish_on)]])]]])



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

(defn tags-component [included-tags on-click]
  [:div.taglist
   [:div.compressor]
   [:div {:on-click on-click}
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

(defn- media-info-comp [current-item on-close]
  [media-item/item-info-component
   {:on-play #(accountant.core/navigate! (explorer-path (:id current-item)))
    :on-close on-close}
   {:selected-item current-item}])

(defn- media-info-modal [state current-item]
  (let [on-close (fn []
                   (session/put! :current-media-item nil)
                   (swap! state assoc :visible-dialog :none))]
    [ui/modal
     {:on-close on-close
      :visible? true}
     [media-info-comp current-item on-close]]))

(defn toolbar [{:keys [sort-by sort-by-clicked detail? detail-clicked tags tags-clicked]}]
  [:div.toolbar.filters
   [toolbar/explorer-buttons {:sort-by sort-by
                              :clicked sort-by-clicked
                              :detail? detail?
                              :detail-clicked detail-clicked}]
   [tags-component tags tags-clicked]])

(defn explorer-page []
  (let [state *state*
        media-items (session/get :media-items)
        sort-by-cursor (r/cursor state [:sort-by])
        current-item (session/cursor [:current-media-item])
        scrollable-items-ref (r/atom nil)]
    (fn []

      (let [included-tags (:included-tags @state)
            media-items (db/items-for-category media-items (get-in @state [:category :id]))
            all-tags (db/unique-tags* media-items)
            media-items (db/items-for-tags media-items included-tags)
            sort-fn (sorter @sort-by-cursor)
            media-items (sort-fn media-items)
            ;visible-dialog (or (and @current-item :media-info) (:visible-dialog @state))
            visible-dialog (:visible-dialog @state)
            searching? (not (empty? (:search-string @state)))
            anim-class :show-scaled-y]
        [:div.explorer
         [:div.media-list
          (when-not searching?
            [toolbar {:sort-by (:sort-by @state)
                      :sort-by-clicked (fn [sort-by]
                                         (let [el @scrollable-items-ref]
                                           (set! (.-scrollTop el)  0)
                                           (swap! state assoc :sort-by sort-by)))
                      :detail? (:detail? @state)
                      :detail-clicked #(swap! state update :detail? not)
                      :tags (:included-tags @state)
                      :tags-clicked #(swap! state assoc :visible-dialog :tag-editor)}])
          (when (:category @state)
            [plawww.categories.categories/category-component (:category @state) {:url (paths/categories-path "")
                                                                                 :index 0
                                                                                 :scale-on-hover? false}])
          (into
           [:ul.items {:ref #(reset! scrollable-items-ref %)}]
           (map-indexed #(m->item %1 %2 {:anim-class anim-class
                                         :category-name (db/any-category-slug %2)
                                         :detail? (:detail? @state)}) media-items))]
         #_[:div.current-item
            (when @current-item
              [media-info-comp @current-item])]

         (case visible-dialog
           :tag-editor
           [tag-editor-modal state {:included-tags included-tags
                                    :all-tags all-tags}]
           :media-info
           [media-info-modal state @current-item]
           nil)]))))

