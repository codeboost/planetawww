;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.media-player.core
  (:require
   [cljsjs.react-player]
   [cljsjs.react-draggable]
   [plawww.media-player.audio-player :as audio-player]
   [plawww.media-player.item-detail :as detail]
   [plawww.media-player.progress-bar :as progress-bar]
   [plawww.utils :as utils]
   [reagent.session :as session]
   [reagent.interop :refer-macros [$ $!]]
   [plawww.paths :as paths]
   [reagent.core :as r]))

(def react-player (r/adapt-react-class js/ReactPlayer))

(defonce mplayer (r/atom nil))

(defn s->ms
  "Seconds to milliseconds."
  [s]
  (* 1000 s))

(defn ms->s
  "Milliseconds to seconds"
  [s]
  (/ s 1000))

(defn- logv
  [& args]
  (js/console.log "media-player: " (apply str args)))

(defn update-state!
  "Saves the state into the session under the `player-state-key`.
  (update-state! conj :show-details? true)"
  [& args]
  (apply (partial session/update-in! [:player-state]) args)
  nil)

(defn- toggle-setting! [k]
  (session/update-in! [:player-state k] not))

(defn- get-setting
  [ks]
  (if (keyword? ks)
    (session/get-in [:player-state ks])
    (session/get-in (into [:player-state] ks))))

(defn send-player-command [command]
  (plawww.media-player.audio-player/command command))

(defn song-progress [progress]
  [:span.song-progress
   (progress-bar/progress-bar
    progress
    (fn [x]
      ()
      (.seekTo @mplayer x)))])

(defn- set-audio-volume [percent]
  (send-player-command {:command :set-volume
                        :percent percent})
  (session/update-in! [:player-state] assoc :volume percent))

(defn time-label [ms-duration progress]
  (let [ms-duration (js/parseFloat ms-duration)
        duration (ms->s ms-duration)]
    [:div.time-label.playback-time.small-text
     (str
      (utils/format-duration (* progress duration))
      "/"
      (utils/format-duration duration))]))

(def state-map
  {:play  :pause
   :pause :play
   :stop  :play})

(defn play-button-text
  "Returns the text to display on the button for the current playback state."
  [s]
  (s {:play  "||"
      :pause ">>"
      :stop  ">>"}))

(defn play-button
  "Play button component.
  Reacts to changes in the [:player-state :playback-state] path.
  Clicking on the button will place a command onto the player command channel.
  The player should then update the [:player-state :playback-state] key."
  []
  (let [ps (session/cursor [:player-state :playback-state])]
       (fn [])
      [:div.accessory-button.play-button {:class (when (= @ps :play) :playing)}
       [:a {:on-click #(session/update-in! [:player-state] assoc :playback-state (or (state-map @ps) :play))}
        (play-button-text (or @ps :pause))]]))

(defn volume-slider-control [progress]
  (progress-bar/progress-bar
   progress
   #(set-audio-volume %)))

(defn player-controls [{:keys [playback-progress playback-duration item volume]}]
  (let [{:keys [title]} item
        duration (if (zero? (or playback-duration 0))
                   (s->ms (js/parseFloat (or (:duration item) 0)))
                   playback-duration)]
    [:div.controls.vstack
     [:div.hstack
      [:div.title-label title]
      [time-label duration playback-progress]]
     [song-progress playback-progress]
     [:div.hstack.bottom-part
      [:div.player-buttons
       [play-button]]
      [:div.button-spacer]
      [:span.volume-control
       [volume-slider-control volume]]]]))

(defn- toggle-accessory-button
  [text key]
  [:div.accessory-button
   {:on-click #(toggle-setting! key)
    :class    (when (get-setting key) :selected)}
   text])

(defn accessory-view []
  [toggle-accessory-button "i" :detail-visible?])

(defn player-view [state]
  [:div.lv-cell.hstack
   [:div.content-area [player-controls state]]
   [:div.accessory-area [accessory-view]]])

(defn player []
  (let [state (session/cursor [:player-state])]
    (fn []
      (let [{:keys [visible detail-visible? item playback-state]} @state
            filename (paths/media-path (:filename item))
            _ (js/console.log "Loading " filename)]
        (if visible
          [:div.player.window.vstack {:class (when detail-visible? :detail)}
           (when detail-visible? [:div.detail
                                  [detail/detail-component item]])
           [react-player {:url filename
                          :playing (= playback-state :play)
                          :ref #(reset! mplayer %)
                          :on-ready (fn [] (js/console.log "READY!"))
                          :on-progress (fn [state]
                                         (js/console.log "state:" state)
                                         (let [percent (.. state -played)
                                               playedSeconds (.. state -playedSeconds)
                                               loadedSeconds (.. state -loadedSeconds)]
                                           (session/update-in! [:player-state] assoc
                                                               :playback-progress percent
                                                               :playback-position (s->ms playedSeconds)
                                                               :playback-duration (s->ms loadedSeconds))))}]




           [:div.toolbar]

           [:div.content [player-view @state]]]
          [:div.player.window.hidden])))))

;(def draggable (r/adapt-react-class js/ReactDraggable))
