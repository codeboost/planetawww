(ns plawww.medialist.explorer
  (:require
   [plawww.paths :as paths]
   [reagent.session :as session]
   [plawww.medialist.toolbar :as toolbar]
   [reagent.core :as r]
   [goog.string :as gstring]
   [goog.string.format]))


(defn format-date [d]
  (gstring/format "%d-%02d-%02d" (.getFullYear d) (.getMonth d) (.getDay d)))

(defn tag-list-comp [tags]
  (into
   [:ul.tags]
   (for [tag tags]
     (let [tag-text (if (= tag (last tags)) tag (str tag ","))]
       [:li
        [:a
         {:href (str "/media/tag/" tag)}
         tag-text]
        " "]))))

(defn m->detail [state {:keys [title id type tags publish_on description_plain] :as m}]
  [:div.item-detail
   [:img {:src (paths/l-image-path id)}]
   [:div.content
    [:h3 title]
    [:p description_plain]]])

(defn m->item [{:keys [title id type tags publish_on description_plain] :as m}]
  [:li.item
   [:a {:href (str "/media/" id)}
    [:span.item-container
     [:img.thumbnail {:src (paths/s-image-path id)}]
     [:span.item-info
      [:div.title title
        [:div.description description_plain]]
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
    :new   (partial sort-by :publish_on #(compare %1 %2))
    :old   (partial sort-by :publish_on #(compare %2 %1))
    (partial sort-by :title)))

(defn parse-dates [media-items]
  (map #(update-in % [:publish_on] (fn [s]
                                     (when s
                                       (js/Date. s)))) media-items))


(comment
 (let [media-items (parse-dates (take 100 (session/get :media-items)))
       sort-fn (sorter :old)
       sorted (sort-fn media-items)]
   (map :title sorted)))




(defn explorer-page []
  (let [state (r/atom {:detailed #{}})
        media-items (take 10 (session/get :media-items))
        media-items (parse-dates media-items)
        sort-by-cursor (r/cursor state [:sort-by])]
    (fn []
      (js/console.log "state=" @state)
      (let [sort-by @sort-by-cursor
            sort-fn (sorter @sort-by-cursor)
            media-items (sort-fn media-items)]
        [:div.explorer
         [toolbar/explorer-buttons state]
         [:span.spacer]
         (into
          [:ul.items]
          (map m->item media-items))
         [:span.spacer]]))))
