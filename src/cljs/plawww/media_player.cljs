(ns plawww.media-player
  (:require [reagent.core :as r]
            [reagent.session :as session]))

(defonce default-state (r/atom {:title ""
                           :image ""
                           :duration 0.0
                           :position 0.0
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

(defn player-controls [state]
  (let [{:keys [title duration position]} state]
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
    [list-view-cell (:image state)
     [player-controls state] [accessory-view]])


(defn get-state []
  (let [{:keys [title]} (session/get :player-state)]
    {:image    "/images/Coperta240.jpg"
     :duration 120
     :position 1.54}))

(defn player []
  (let [cursor (session/cursor [:player-state])]
    (fn []
      [:div.player.window.vstack
       [:div.toolbar]
       [:div.content
        [player-view @cursor]]])))

(comment

  (session/update-in! [:player-state] merge {:title    "Resetat"
                                             :image    "/images/Coperta240.jpg"
                                             :duration 120
                                             :position 0.3} )

  (def cana {:title    "Cana cu apa"
             :image    "/images/Coperta240.jpg"
             :duration 120
             :position 0.3})

  (session/reset!      {:title    "Ce mai zicem oare ?"
                        :image    "/images/Coperta240.jpg"
                        :duration 120
                        :position 1.54}))

