(ns plawww.media-player.item-detail
  (:require [reagent.core :as r]
            [plawww.ui :as ui]
            [plawww.paths :as paths]
            [reagent.session :as session]
            [clojure.string :as str]
            [plawww.utils :as utils]
            [cljs.test :refer-macros [deftest is testing run-tests]]
            [cljs.core.async :refer [put!]]
            [reagent.interop :refer-macros [$ $!]]))

(defn image-path [item]
  (let [id (:id item)]
    (paths/s-image-path id)))

(defn detail-image-path [item]
  (let [id (:id item)]
    (paths/l-image-path id)))

(defn item-href [id]
  (str "/media/" id))

(defn item-content [{:keys [title id description]}]
  [:div.item-content
   [:div.title [:a {:href (item-href id)} title]]
   [:div.description (or description "")]])

(defn item->detail-item [item]
  ^{:key (:id item)}
  [:li.media-item.detail
   [ui/list-view-cell
    (image-path item)
    [item-content item]
    [:div.item-accessory]]])

(defn duration-comp [duration played]
  [:div.times
   [:span.played (utils/format-duration (* played duration))]
   [:span " / "]
   [:span.duration (utils/format-duration duration)]])

(defn tag-list-comp [state]
  (fn []
    (into
     [:div.tags]
     (for [tag (get-in @state [:item :tags])]
       [:span
        [:a
         {:href (str "/media/tag/" tag)
          :on-click #(swap! state assoc :detail-visible? false)}
         tag]
        " "]))))

(defn detail-component [state]
  (fn []
    (let [{:keys [duration item played]} @state
          duration (if (zero? duration) (or (:duration item) 0) duration)]
      [:div.media-item-detail
       [:div.detail-info
        [:div.top-part
         [:div.info-container
          [:div.title (:title item)]
          [duration-comp duration played]
          [tag-list-comp state]]]
        [:div.description (:description_plain item)]]])))