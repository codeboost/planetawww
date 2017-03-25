(ns plawww.media-player
  (:require [reagent.core :as r]
            [reagent.session :as session]))

(defonce default-state (r/atom {:item           {:title "" :image ""}
                                :duration       0.0
                                :position       0.0
                                :playback-state :stopped}))


(defn list-view-cell[image content accessory-view]
  [:div.lv-cell.hstack
   [:div.image-area
    [:img.image {:src image}]]
   [:div.content-area content]
   [:div.accessory-area accessory-view]])


(defn progress-bar [progress]
  [:div.progress-bar
   [:div.progress-bar-progress
    {:style {:width (str (* 100 (min 1 progress)) "%")}}]])

(defn format-time [timestamp]
  (str timestamp "s"))

(defn time-label [timestamp]
  [:div.grow2.time-label.playback-time (format-time timestamp)])

(defn player-controls [state item]
  (let [{:keys [duration position]} state
        {:keys [title]} item]
    [:div.controls.vstack
     [:div.hstack
      [:div.title-label.grow8 title]
      [time-label (* position duration)]]
     [progress-bar position]
     [:div.hstack.bottom-part
      [:div.player-buttons.grow8.hstack
       [:div.button.play-button [:a {:href "#"} "PLAI"]]
       [:div.sp]]
      [time-label duration]]]))


(defn accessory-view [])

(defn player-view [state]
  (let [item (:item state)]
    [list-view-cell (:image item)
     [player-controls state item] [accessory-view]]))

(defn player []
  (let [player-state (session/cursor [:player-state])]
    (fn []
      [:div.player.window.vstack
       [:div.toolbar]
       [:div.content
        [player-view @player-state]]])))

(comment

  (session/update-in! [:player-state] merge {:item     {:title "Resetat"
                                                        :image "/data/images/media/9l.jpg"}
                                             :duration 120
                                             :position 0.3})

  )
