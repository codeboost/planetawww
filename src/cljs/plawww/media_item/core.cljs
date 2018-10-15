(ns plawww.media-item.core
  (:require [reagent.core :as r]
            [plawww.paths :as paths]
            [plawww.components.core :as felurite]
            [plawww.media-player.core :as media-player]))

(defn artwork-bg-image [url]
  (str "url(" url  "), radial-gradient(#14fdce, #000),  repeating-linear-gradient(transparent 0,rgba(0,0,0,0.1) 2px,transparent 4px)"))

(defn toolbar-item [title on-click]
  [:div.toolbar-item
   {:on-click on-click}
   title])

(defn item-info-component [state]
  (fn [_]
    (let [{:keys [selected-item item-info-visible?]} @state]
      (when item-info-visible?
        [:div.media-item-info-container
         [felurite/minimise-button state "x" :item-info-visible?]
         [:div.scroll-container
          [:div.media-item-info
           [:div.title (:title selected-item)]
           [:div.album-art-container
            [:div.album-art
             [:div.img-container
              {:style
               {:background-image (artwork-bg-image (paths/l-image-path (:id selected-item)))}}]]]
           [:div.description (:description_plain selected-item)]]]
         [:div.toolbar
          [toolbar-item "PLAY" (fn []
                                 (media-player/set-current-item selected-item)
                                 (swap! state assoc :item-info-visible? false))]
          [toolbar-item "ECOURI" (fn [])]]]))))


