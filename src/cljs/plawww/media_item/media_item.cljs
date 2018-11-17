;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.media-item.media-item
  (:require [reagent.core :as r]
            [plawww.components.components :refer [tag-list-component]]
            [plawww.mediadb.core :as db]
            [plawww.paths :as paths]))

(defn artwork-bg-image [url]
  (str "url(" url ")"))

(defn toolbar-item [title on-click]
  [:div.toolbar-item
   {:on-click on-click}
   title])

(defn info-component [{:keys [title tags id description_plain type] :as item}]
  [:div.media-item-info
   [:div.title title]
   [tag-list-component tags #()]
   [:div.album-art-container
    [:div.album-art
     [:div.img-container
      {:style
       {:background-image (artwork-bg-image (paths/media-image-path id {:show-custom? (= type "video")
                                                                        :category-name (db/any-category-slug item)
                                                                        :size :large}))}}]]]
   [:div.description description_plain]])

(defn item-info-component [{:keys [on-play on-close]} _]
  (let [state (r/atom {:section :info})]
    (fn [_ {:keys [selected-item]}]
      [:div.media-item-info-container
       [:div.min-button [:a {:href :#
                             :on-click on-close} "x"]]
       [:div.scroll-container
        (case (:section @state)
          :info
          [info-component selected-item]
          :ecouri
          [:div {:style {:padding "10px"
                         :font-size "18px"}}
           [:h3 "ECOURI"]
           [:p "Inca nu-i gata. Dar poti sa ne scrii un e-mail:"]
           [:p
            [:a {:href "mailto:planetamoldova@planetamoldova.net"} "planetamoldova@planetamoldova.net"]]
           [:p "Sau pe twitter:"]
           [:p
            [:a {:href "https://twitter.com/planetamoldova_"
                 :target "_new-twitter"} "https://twitter.com/planetamoldova_"]]])]
       [:div.toolbar
        [toolbar-item "PLAY" on-play]
        (case (:section @state)
          :info
          [toolbar-item "ECOURI" #(swap! state assoc :section :ecouri)]
          :ecouri
          [toolbar-item "INFO" #(swap! state assoc :section :info)])]])))



