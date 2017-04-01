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

(defn item-href [item]
  (str "/media/" (:id item)))

(defn item-content [item]
  [:div.item-content
   [:div.title
    [:a {:href (item-href item)}
     (:title item)]]
   [:div.description (or (:description item) "")]])


(defn item->detail-item [item]
  ^{:key (:id item)} [:li.detail-media-item
   [ui/list-view-cell
    (image-path item)
    [item-content item]
    [:div.item-accessory]]])


(defonce KOLBAS (js->clj js/kolbasulPlanetar :keywordize-keys true))
(defonce THE-MEDIA (:media KOLBAS))

(defn test-detail-item [q]
  [:ul.media-items.justify-left
   (map item->detail-item (take 200 THE-MEDIA))])