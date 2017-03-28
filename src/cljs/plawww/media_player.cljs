(ns plawww.media-player
  (:require [reagent.core :as r]
            [reagent.session :as session]
            [clojure.string :as str]
            [goog.string :as gstring]
            [goog.string.format]
            [cljs.core.async :refer [put!]]
            [reagent.interop :refer-macros [$ $!]]))

(defn list-view-cell[image content accessory-view]
  [:div.lv-cell.hstack
   [:div.image-area
    [:img.image {:src image}]]
   [:div.content-area content]
   [:div.accessory-area accessory-view]])


(defn send-player-command [command]
  (when-let [channel (session/get-in [:audio-player-control-channel])]
    (put! channel command)))

(defn percent-width [object x]
  (let [width ($ object width)]
    (cond (pos? width) (/ x width)
          :else 0)))

(defn progress-bar [progress]
  [:div.progress-bar {:on-click (fn [e]
                                    (let [xthis (r/current-component)
                                          target (js/$ ($ e :target))
                                          pagex ($ e :pageX)
                                          offset ($ target offset)
                                          offsetLeft ($ offset :left)
                                          offsetx (- pagex offsetLeft)
                                          percent (percent-width target offsetx)]
                                      (print "clicked: " percent ", this:" xthis ", target:" ($ e :target))
                                      (send-player-command {:command :set-pos
                                                            :percent percent})))}
   [:div.progress-bar-progress
    {:style {:width (str (* 100 (min 1 progress)) "%")}}]])

(defn format-time [timestamp]
  (let [minutes (quot timestamp 60)
        seconds (mod timestamp 60)]
    (gstring/format "%02d:%02d" minutes seconds)))

(defn time-label [timestamp]
  [:div.grow2.time-label.playback-time (format-time timestamp)])


(defn play-button-text [state]
  (let [txt (state {:play "PAUZA"
                    :pause "PORN."
                    :stop "PORN."})] txt))

(def state-map
  {:play :pause
   :pause :play
   :stop :play})

(defn play-button-click
  "`ps` is an atom with the playback state
  `pc` is the channel to which to send the commands"
  [pc ps]
  (fn [e]
    (.preventDefault e)
    (put! @pc (or (state-map @ps) :play))))

(defn play-button
  "Play button component.
  Reacts to changes in the [:player-state :playback-state] path.
  Clicking on the button will place a command onto the player command channel.
  The player should then update the [:player-state :playback-state] key."
  []
  (let [ps (session/cursor [:player-state :playback-state])
        pc (session/cursor [:audio-player-control-channel])]
    (fn []
      [:div.button.play-button
       [:a {:on-click (play-button-click pc ps)}
        (play-button-text (or @ps :pause))]])))

(defn player-controls [state item]
  (let [{:keys [position]} state
        {:keys [duration title]} item
        duration (js/parseFloat duration)]
    [:div.controls.vstack
     [:div.hstack
      [:div.title-label.grow8 title]
      [time-label duration]]
     [progress-bar position]
     [:div.hstack.bottom-part
      [:div.player-buttons.grow8.hstack
       [play-button]
       [:div.sp]]
      (when (pos? position) [time-label (* position duration)])]]))


(defn accessory-view [])

(defn player-view [state]
  (let [item (:item state)]

    [list-view-cell (:image item)
     [player-controls state item] [accessory-view]]))

(defn player []
  (let [player-state (session/cursor [:player-state])]
    (fn []
      (if (and @player-state (@player-state :visible))
        [:div.player.window.vstack
         [:div.toolbar]
         [:div.content
          [player-view @player-state]]]
        [:div.player.window.hidden]))))



(comment
  (format-time 100.76242857142857)
  (session/update-in! [:player-state] merge {:item     {:title "Resetat"
                                                        :image "/data/images/media/9l.jpg"}
                                             :duration 120
                                             :position 0.3})

  )
