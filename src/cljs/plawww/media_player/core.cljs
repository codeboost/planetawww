;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.media-player.core
  (:require
   [cljs.core.async :refer [put!]]
   [cljsjs.react-draggable]
   [clojure.string :as str]
   [reagent.core :as r]
   [reagent.session :as session]
   [reagent.interop :refer-macros [$ $!]]
   [plawww.media-player.item-detail :as detail]
   [plawww.media-player.progress-bar :as progress-bar]
   [plawww.ui :as ui]
   [plawww.utils :as utils]))

(def draggable (r/adapt-react-class js/ReactDraggable))

(defn- logv
  [& args]
  (println "media-player: " (apply str args)))

(defonce player-state-key :player-state)

(defn update-state!
  "Saves the state into the session under the `player-state-key`.
  (update-state! conj :show-details? true)"
  [& args]
  (apply (partial session/update-in! [player-state-key]) args)
  nil)

(defn- toggle-setting! [k]
  (session/update-in! [player-state-key k] not))

(defn- save-setting!
  [k v]
  (update-state! assoc k v))

(defn- get-setting
  [ks]
  (let [ks (if (keyword? ks) [ks] ks)]
    (session/get-in (into [player-state-key] ks))))

(defn send-player-command [command]
  (when-let [channel (session/get-in [:audio-player-control-channel])]
    (put! channel command)))

(defn song-progress [progress]
  [:span.song-progress
   (progress-bar/progress-bar
    progress
    #(send-player-command {:command :set-pos
                           :percent %}))])


(defn- set-audio-player-volume [percent]
  (send-player-command {:command :set-volume
                         :percent percent})
  (session/update-in! [:player-state] assoc :volume percent))


(defn time-label [duration position]
  [:div.grow2.time-label.playback-time
   (str
    (utils/format-duration (* position duration))
    "/"
    (utils/format-duration duration))])


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


(defn- toggle-accessory-button
  [text key]
  (let [className (when (get-setting key) "selected")]
    (logv "className: " className)
    [:div.accessory-button
     {:on-click #(toggle-setting! key)
      :class className}
     text]))


(defn volume-slider-control [progress]
  (progress-bar/progress-bar
   progress
   #(set-audio-player-volume %)))

(defn volume-control []
  [:span.volume-control
     [volume-slider-control (get-setting :volume)]])

(defn player-controls [state item]
  (let [{:keys [position]} state
        {:keys [duration title]} item
        duration (js/parseFloat duration)]
    [:div.controls.vstack
     [:div.hstack
      [:div.title-label.grow8 title]
      [:span
       [time-label duration position]]]
     [song-progress position]
     [:div.hstack.bottom-part
      [:div.player-buttons
       [play-button]
       [volume-control]]]]))




(defn list-view-cell[image content accessory-view]
  [:div.lv-cell.hstack
   [:div.content-area content]
   [:div.accessory-area accessory-view]])

(defn accessory-view []
  [toggle-accessory-button "i" :detail-visible?])

(defn player-view [state]
  (let [item (:item state)]
    [list-view-cell (:image item)
     [player-controls state item] [accessory-view]]))


(defn player-view2 [state]
  (let [item (:item state)]
    [list-view-cell (:image item)
     [player-controls state item] [accessory-view]]))

(defn item-details-area [astate]
  (if (@astate :detail-visible?)
    [:div.detail
     [detail/detail-component (@astate :item)]]
    [:div.detail.hidden]))

(defn player []
  (let [player-state (session/cursor [player-state-key])]
    (fn []
      (if (:visible @player-state)
         [:div.player.window.vstack {:class (when (@player-state :detail-visible?) "detail")}
          [item-details-area player-state]
          [:div.toolbar]
          [:div.content
           [player-view @player-state]]]
         [:div.player.window.hidden]))))




(comment
  (format-time 100.76242857142857)
  (session/update-in! [:player-state] merge {:item     {:title "Resetat"
                                                        :image "/data/images/media/9l.jpg"}
                                             :duration 120
                                             :position 0.3}))


