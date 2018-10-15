(ns plawww.media-item.media-item
  (:require [reagent.core :as r]
            [plawww.paths :as paths]
            [plawww.components.components :as felurite]
            [plawww.media-player.core :as media-player]))

(defn artwork-bg-image [url]
  (str "url(" url  "), radial-gradient(#14fdce, #000),  repeating-linear-gradient(transparent 0,rgba(0,0,0,0.1) 2px,transparent 4px)"))

(defn toolbar-item [title on-click]
  [:div.toolbar-item
   {:on-click on-click}
   title])

(defn item-info-component [{:keys [on-play]} _]
  (fn [_ {:keys [selected-item]}]
    [:div.media-item-info-container
     [:div.min-button [:a {:href "/explorer"} "x"]]
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
      [toolbar-item "PLAY" on-play]
      [toolbar-item "ECOURI" (fn [])]]]))


