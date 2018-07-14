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
   [plawww.media-player.item-detail :as detail]
   [plawww.media-player.progress-bar :as progress-bar]
   [plawww.utils :as utils]
   [reagent.session :as session]
   [reagent.interop :refer-macros [$ $!]]
   [plawww.paths :as paths]
   [reagent.core :as r]))

(def react-player (r/adapt-react-class js/ReactPlayer))

(defonce mplayer (r/atom nil))

(defonce mplayer-state (r/atom {:playing false
                                :url nil
                                :volume 0.7
                                :muted false
                                :played 0
                                :loaded 0
                                :duration 0
                                :playback-rate 0
                                :loop false
                                :item nil
                                ;lump them all together
                                :detail-visible? false
                                :volume-visible? false}))

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

(defn set-current-item [item]
  (swap! mplayer-state merge {:item item
                              :visible true
                              :detail-visible? true
                              :playing true}))

(defn- set-audio-volume [percent]
  (swap! mplayer-state assoc :volume percent))


(defn time-label [ms-duration progress]
  (let [ms-duration (js/parseFloat ms-duration)
        duration (ms->s ms-duration)]
    [:div.time-label.playback-time.small-text
     (str
      (utils/format-duration (* progress duration))
      "/"
      (utils/format-duration duration))]))

(defn play-button
  "Play button component.
  Reacts to changes in the [:player-state :playback-state] path.
  Clicking on the button will place a command onto the player command channel.
  The player should then update the [:player-state :playback-state] key."
  []
  (fn [state]
    (let [playing? (:playing @state)
          text (if playing? "||" ">>")]
      [:div.accessory-button.play-button
       {:class (when playing? :selected)
        :on-click #(swap! state update :playing not)}
       text])))


(defn song-progress [state]
  (fn []
    (let [{:keys [played]} @state]
      [:span.song-progress
       (progress-bar/progress-bar played #(.seekTo @mplayer %))])))

(defn- toggle-accessory-button
  [state text key]
  [:div.accessory-button
   {:on-click #(swap! state update-in [key] not)
    :class    (when (@state key) :selected)}
   text])

(defn- minimise-button
  [state text key]
  [:div.min-button
   {:on-click #(swap! state update key not)
    :style {:cursor :pointer}}
   text])

(defn media-player [state]
  (fn []
    (let [{:keys [item playing volume]} @state]
      [:div.pm-media-player
       [react-player
        {:url (paths/item-path item)
         :class-name :react-player
         :width "100%"
         :height "100%"
         :playing playing
         :volume volume
         :ref #(reset! mplayer %)
         :on-ended #(js/console.log "Gata!")
         :on-ready #(js/console.log "react-player: ready.")
         :on-play #(swap! state assoc :playing true)
         :on-pause #(swap! state assoc :playing false)
         :on-error #(swap! state merge {:playing false :error %})
         :on-duration #(swap! state assoc :duration %)
         :on-progress #(swap! state assoc :played (.. % -played))}]])))


(defn- volume-text [percent]
  (cond
    (< percent 0.01) "   "
    (and (>= percent 0.1) (< percent 0.3)) " . "
    (and (>= percent 0.3) (< percent 0.6)) " v "
    (and (>= percent 0.6) (< percent 0.9)) " V "
    (> percent 0.9) "+V+"))



(defn volume-control
  "Fancy volume control.
  By default a toggle accessory is rendered, which, when clicked will display a vertical volume selector
  allowing the human to set the volume."
  [state]
  (let [ls (r/atom {:progress-visible? false})]
    (fn []
      (let [{:keys [volume]} @state]
        [:div.volume-control
         [toggle-accessory-button ls (volume-text volume) :progress-visible?]
         (when (:progress-visible? @ls)
           [:div.volume-selector
            [progress-bar/vertical-progress-bar
             volume
             #(swap! state assoc :volume %)]
            [:div.faker]])]))))

(defn min-player [state]
  (fn []
    [:div.min-player
     [:div.controls
      [play-button state]
      [song-progress state]
      [toggle-accessory-button state "i" :detail-visible?]
      [volume-control state]]]))

(defn player []
  (let [state mplayer-state]
    (fn []
      (let [{:keys [visible item detail-visible?]} @state]
        (if visible
          [:div.player.window.vstack {:class (when detail-visible? :detail-visible)}
           [:div.detail
            [minimise-button state "x" :detail-visible?]
            [detail/detail-component item]
            [media-player state]]
           [:div.toolbar]
           [:div.content
            [min-player state]]]
          [:div.player.window.hidden])))))