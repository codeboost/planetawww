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
  ^{:key (:id item)} [:li.detail-media-item]
   [ui/list-view-cell
    (image-path item)
    [item-content item]
    [:div.item-accessory]])

(defn duration-comp [{:keys [duration]}]
  [:div.duration (utils/format-duration duration)])

(defn tag-list-comp [{:keys [tags]}]
  [:div.tags
   (when (not-empty tags)
     (str/join ", " tags))])

(deftest detail-comp-tests
  (is (= [:div.tags]
         (tag-list-comp {})))

  (is (= [:div.tags "one"]
         (tag-list-comp {:tags ["one"]})))

  (is (= [:div.tags "one, two, three"]
         (tag-list-comp {:tags ["one" "two" "three"]}))))

(defn detail-component [item]
  [:div.media-item-detail
   [:div.detail-info
    [:div.top-part
     [:div.info-container
      [:div.title (:title item)]
      [duration-comp item]
      [tag-list-comp item]]]
    [:div.description (:description_plain item)]]])