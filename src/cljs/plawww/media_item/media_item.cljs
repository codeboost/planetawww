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


(defn img-container [_]
  (let [dom-el (r/atom nil)
        transform-style (r/atom nil)
        resize-proportionally (fn []
                                (let [el @dom-el
                                      w (.-offsetWidth el)
                                      h (.-offsetHeight el)]
                                  (js/console.log w "x" h)
                                  (if (and (pos? w) (pos? h))
                                    (let [scale-x (when (> w h) (/ h w))
                                          scale-y (when (> h w) (/ w h))
                                          scale-x (when scale-x (str "scaleX(" scale-x ")"))
                                          scale-y (when scale-y (str "scaleY(" scale-y ")"))]
                                      (reset! transform-style (or scale-x scale-y))
                                      (js/console.log scale-x "-" scale-y))
                                    (reset! transform-style nil))))]

      (r/create-class
       {:component-did-mount
        (fn []
          (js/console.log "did-mount")
          (.addEventListener js/window "resize" resize-proportionally)
          (resize-proportionally))

        :component-did-update
        (fn [_]
          (js/console.log "did-update")
          (resize-proportionally))

        :component-will-unmount
        (fn []
          (js/console.log "will-unmount")
          (.removeEventListener js/window "resize" resize-proportionally))
        :reagent-render
        (fn [{:keys [title tags id description_plain type] :as item}]
          [:div.img-container {:ref #(reset! dom-el %)}
           [:img {:src (paths/media-image-path id {:show-custom? (= type "video")
                                                   :category-name (db/any-category-slug item)
                                                   :size :large})
                  :style {:transform @transform-style}}]])})))



(defn info-component [{:keys [title tags id description_plain type] :as item} & [{:keys [show-details?] :or
                                                                                        {show-details? true}}]]
  [:div.media-item-info
   [:div.title title]
   [tag-list-component tags #()]
   (when show-details?
     [:div.album-art-container
      [img-container item]])
   (when show-details?
     [:div.description description_plain])])

(defn feedback-component []
  [:div {:style {:padding "10px"
                 :font-size "18px"}}
   [:h3 "ECOURI"]
   [:p "Inca nu-i gata. Dar poti sa ne scrii un e-mail:"]
   [:p
    [:a {:href "mailto:planetamoldova@planetamoldova.net"} "planetamoldova@planetamoldova.net"]]
   [:p "Sau pe twitter:"]
   [:p
    [:a {:href "https://twitter.com/planetamoldova_"
         :target "_new-twitter"} "https://twitter.com/planetamoldova_"]]])

(defn item-info-component [{:keys [on-play on-close]} _]
  (let [state (r/atom {:section :info})]
    (fn [_ {:keys [selected-item]}]
      [:div.media-item-info-container
       (case (:section @state)
         :info
         [info-component selected-item]
         :ecouri
         [feedback-component])

       [:div.toolbar
        [toolbar-item "PLAY" on-play]
        (case (:section @state)
          :info
          [toolbar-item "ECOURI" #(swap! state assoc :section :ecouri)]
          :ecouri
          [toolbar-item "INFO" #(swap! state assoc :section :info)])]])))



