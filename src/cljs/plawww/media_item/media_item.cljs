;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.media-item.media-item
  (:require [reagent.core :as r]
            [plawww.components.components :refer [tag-list-component]]
            [plawww.paths :as paths]))

(defn artwork-bg-image [url]
  (str "url(" url ")"))

(defn toolbar-item [title on-click]
  [:div.toolbar-item
   {:on-click on-click}
   title])

(defn info-component [{:keys [title tags id description_plain type]}]
  [:div.media-item-info
   [:div.title title]
   [tag-list-component tags #()]
   [:div.album-art-container
    [:div.album-art
     [:div.img-container
      {:style
       {:background-image (artwork-bg-image (paths/l-image-path id (= type "video")))}}]]]
   [:div.description description_plain]])

(defn item-info-component [{:keys [on-play]} _]
  (let [state (r/atom {:section :info})]
    (fn [_ {:keys [selected-item]}]
      [:div.media-item-info-container
       [:div.min-button [:a {:href (paths/explorer-path "")} "x"]]
       [:div.scroll-container
        (case (:section @state)
          :info
          [info-component selected-item]
          :ecouri
          [:div
           [:p "Nu, nu poti sa postezi aici nimic. Nici comentariile altor persoane nu-s. Nici cate Like-uri sau View-uri."]
           [:p "Da ce-ti trebuie ? Doar nu vrei sa vezi ce cred altii ca sa crezi si tu la fel ?"]
           [:p "Scrie-ne un mail. Noua, autorilor. Vrem parerea ta cruda, fara pribambasuri si network effects.
           Este foarte probabil ca iti vom raspunde."]
           [:p [:a {:href "mailto:planetamoldova@planetamoldova.net"} "planetamoldova@planetamoldova.net"]]])]
       [:div.toolbar
        [toolbar-item "PLAY" on-play]
        (case (:section @state)
          :info
          [toolbar-item "ECOURI" #(swap! state assoc :section :ecouri)]
          :ecouri
          [toolbar-item "INFO" #(swap! state assoc :section :info)])]])))



