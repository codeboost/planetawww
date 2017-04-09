(ns plawww.media-item-detail
  (:require [reagent.core :as r]
            [plawww.ui :as ui]
            [plawww.paths :as paths]
            [reagent.session :as session]
            [clojure.string :as str]
            [cljs.core.async :refer [put!]]
            [reagent.interop :refer-macros [$ $!]]))


(defn image-path [item]
  (let [id (:id item)]
    (paths/s-image-path id)))

(defn item-href [id]
  (str "/media/" id))

(defn item-content [{:keys [title id description]}]
  [:div.item-content
   [:div.title [:a {:href (item-href id)} title]]
   [:div.description (or description "")]])

(defn item->detail-item [item]
  ^{:key (:id item)} [:li.detail-media-item
   [ui/list-view-cell
    (image-path item)
    [item-content item]
    [:div.item-accessory]]])
