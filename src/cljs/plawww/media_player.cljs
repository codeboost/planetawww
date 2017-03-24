(ns plawww.media-player)


(defn list-view-cell[image content accessory-view]
  [:div.lv-cell.hstack
   [:div.image-area
    [:img.image {:src image}]]
   [:div.content-area content]
   [:div.accessory-area accessory-view]])


(defn progress-bar [progress]
  [:div.progress-bar
   [:div.progress-bar-progress
    {:style {:width (str (* 100 progress) "%")}}]])


(defn player-controls [title]
  [:div.controls.vstack
   [:div.hstack
    [:div.title-label.grow8 title]
    [:div.grow2.time-label.playback-time "1:16"]]
   [progress-bar 0.4]
   [:div.hstack.bottom-part
    [:div.player-buttons.grow8.hstack
     [:div.button.play-button [:a {:href "#"} "PLAY"]]
     [:div.sp]]
    [:div.grow2.time-label.total-time "2:20"]]])


(defn player-view [image title accessory]
  [list-view-cell image
   [player-controls title] accessory])

(defn accessory-view []
  )

(defn player[]
  [:div.player.window.vstack
   [:div.toolbar]
   [:div.content
    [player-view "/images/lidasicolea.jpg" "Biznis pas cu pas 1" [accessory-view]]]])