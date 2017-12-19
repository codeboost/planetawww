;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.media-player
  (:require
   [cljs.core.async :refer [put!]]
   [cljsjs.react-draggable]
   [clojure.string :as str]
   [reagent.core :as r]
   [reagent.session :as session]
   [reagent.interop :refer-macros [$ $!]]
   [plawww.media-item-detail :as detail]
   [plawww.ui :as ui]
   [plawww.utils :as utils]))

(def draggable (r/adapt-react-class js/ReactDraggable))

(defn logv
  [& args]
  (println "media-player: " (apply str args)))


(defn send-player-command [command]
  (when-let [channel (session/get-in [:audio-player-control-channel])]
    (put! channel command)))

(defn percent-width [object x]
  (let [width ($ object width)]
    (cond (pos? width) (/ x width)
          :else 0)))

(defn progress-bar [progress callback]
  [:div.progress-bar {:on-click (fn [e]
                                    (let [_this (r/current-component)
                                          target (js/$ ($ e :target))
                                          pagex ($ e :pageX)
                                          offset ($ target offset)
                                          offsetLeft ($ offset :left)
                                          offsetx (- pagex offsetLeft)
                                          percent (percent-width target offsetx)]
                                      (print "clicked: " percent ", this:" _this ", target:" ($ e :target))
                                      (callback percent)))}
   [:div.progress-bar-progress
    {:style {:width (str (* 100 (min 1 progress)) "%")}}]])

(defn song-progress [progress]
  [:span.song-progress
   (progress-bar
    progress
    #(send-player-command {:command :set-pos
                           :percent %}))])

(defn volume-progress [progress]
  (logv "volume-progress: " progress)
  [:span.volume
   (progress-bar
    progress
    #(send-player-command {:command :set-volume
                           :percent %}))])

(defn time-label [timestamp]
  [:div.grow2.time-label.playback-time (utils/format-duration timestamp)])


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
  (let [{:keys [position volume]} state
        {:keys [duration title]} item
        duration (js/parseFloat duration)]
    [:div.controls.vstack
     [:div.hstack
      [:div.title-label.grow8 title]
      [time-label duration]]
     [song-progress position]
     [:div.hstack.bottom-part
      [:div.player-buttons.grow8.hstack
       [play-button]
       [:div.sp]]
      [volume-progress volume]]]))


(defn list-view-cell[image content accessory-view]
  [:div.lv-cell.hstack
   [:div.content-area content]
   [:div.accessory-area accessory-view]])


(defn accessory-button-click-handler []
  (fn [e]
    (print "click handler")
    (.preventDefault e)
    (session/update-in! [:player-state :detail-visible] not)))

(defn accessory-view []
  [:div.accessory-button {:on-click (accessory-button-click-handler)} "i"])


(defn player-view [state]
  (let [item (:item state)]
    [list-view-cell (:image item)
     [player-controls state item] [accessory-view]]))


(defn player-view2 [state]
  (let [item (:item state)]
    [list-view-cell (:image item)
     [player-controls state item] [accessory-view]]))

(defn item-details-area [astate]
  (if (@astate :detail-visible)
    [:div.detail
     [detail/detail-component (@astate :item)]]
    [:div.detail.hidden]))

(defn player []
  (let [player-state (session/cursor [:player-state])]
    (fn []
      (logv "state: " @player-state)
      [draggable
       {:grid [25 25]}
       (if (:visible @player-state)
          [:div.player.window.vstack {:class (when (@player-state :detail-visible) "detail")}
           [item-details-area player-state]
           [:div.toolbar]
           [:div.content
            [player-view @player-state]]]
          [:div.player.window.hidden])])))




(comment
  (format-time 100.76242857142857)
  (session/update-in! [:player-state] merge {:item     {:title "Resetat"
                                                        :image "/data/images/media/9l.jpg"}
                                             :duration 120
                                             :position 0.3}))


