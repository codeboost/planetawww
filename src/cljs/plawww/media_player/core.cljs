;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.media-player.core
  (:require
   [cljsjs.react-player]
   [plawww.media-player.item-detail :as detail]
   [plawww.media-player.progress-bar :as progress-bar]
   [plawww.utils :as utils]
   [reagent.interop :refer-macros [$ $!]]
   [plawww.paths :as paths]
   [reagent.core :as r]))

(def react-player (r/adapt-react-class js/ReactPlayer))

(defonce mplayer (r/atom nil))

(defonce mplayer-state (r/atom {:height 320
                                :playing false
                                :url nil
                                :volume 0.7
                                :muted true ;Safari restriction. First time must be muted. Reset when user clicks play
                                :played 0
                                :loaded 0
                                :duration 0
                                :playback-rate 0
                                :loop false
                                :item nil
                                ;lump them all together
                                :detail-visible? false
                                :volume-visible? false
                                :fullscreen? false}))

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
  (let [muted (:muted @mplayer-state)
        playing? (not muted)]
    (swap! mplayer-state merge {:item item
                                :visible true
                                :detail-visible? true
                                :playing playing?}))
  (reagent.core/flush))

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

(defn- flush-play!
  "Call when muted is true.
  Resets `muted` to false and sets `playing` to true, then calls `reagent.core/flush` to trigger
  an immediate re-render.
  This is necessary for Safari which requires first time playback to be started from a click event handler."
  [state]
  (swap! state merge {:muted false
                      :playing true})
  (reagent.core/flush))

(defn play-button
  "Play button component.
  Reacts to changes in the [:player-state :playback-state] path.
  Clicking on the button will place a command onto the player command channel.
  The player should then update the [:player-state :playback-state] key."
  []
  (fn [state]
    (let [{:keys [playing muted]} @state
          text (if playing "||" ">>")]
      [:div.accessory-button.play-button
       {:class (when playing :selected)
        :on-click (fn []
                    (if muted ; Safari restriction - media must be loaded in a muted state.
                      (flush-play! state)
                      (swap! state update :playing not)))}

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

(defn- start-update-duration-timer
  "update-interval is an atom which will contain the interval id.
   state is the atom where the :duration and :played values will be stored."
  [mplayer state update-interval]
  (reset!
   update-interval
   (js/setInterval
    #(let [current-time (.getCurrentTime @mplayer)
           duration     (.getDuration @mplayer)
           played       (if (pos? duration) (/ current-time duration) 0)]
       (swap! state merge {:played played :duration duration}))
    500)))

;This hack is necessary in order to adjust the media player height, which fights me
;if I try to do it with CSS. I wish I didn't have to do it though.
(defn- adjust-player-dimensions!
  [container-el state]
  (let [parent-height (.height (js/$ (.-parentNode @container-el)))
        parent-height (Math/round (* 0.88 parent-height))]
    (swap! state assoc :height parent-height)))

(defn media-player [state]
  (let [update-interval (r/atom 0)
        container-el (r/atom nil)
        resize-handler #(adjust-player-dimensions! container-el state)]
    (r/create-class
     {:component-did-mount
      (fn []
        (start-update-duration-timer mplayer state update-interval)
        (adjust-player-dimensions! container-el state)
        (.addEventListener js/window "resize" resize-handler))

      :component-will-unmount (fn []
                                (js/clearInterval @update-interval)
                                (.removeEventListener js/window "resize" resize-handler))

      :reagent-render
      (fn []
        (let [{:keys [item playing volume muted height fullscreen?]} @state
              audio? (= (:type item) "audio")]
          [:div.pm-media-player
           {:ref #(reset! container-el %)}
           [react-player
            {:url (paths/item-path item)
             :class-name :react-player
             :width "100%"
             :height (if audio? 0 height)
             :muted muted
             :playing playing ;Not playing if muted
             :volume volume
             :ref #(reset! mplayer %)
             :on-start (fn []
                         (if muted ; Safari restriction - media must be loaded in a muted state.
                           (flush-play! state)
                           (swap! state update :playing true)))
             :on-ended #(swap! state update :playing false)
             :on-ready #(js/console.log "react-player: ready.")
             :on-play #(swap! state assoc :playing true)
             :on-pause #(swap! state assoc :playing false)
             :on-error (fn [err]
                         (swap! state merge {:playing false :error err})
                         (js/console.log "Error: " err))
             :on-duration #(swap! state assoc :duration %)
             :on-progress (fn [p]
                            ;don't do this - scrolling won't work
                            #_(swap! state assoc :played (.. p -played)))}]]))})))


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

(defn toolbar-item [title on-click]
  [:div.toolbar-item
   [:a {:href "#"
        :on-click on-click}
    title]])

(defn player []
  (let [state mplayer-state]
    (fn []
      (let [{:keys [visible item detail-visible? duration played]} @state
            audio? (= (:type item) "audio")]
        (if visible
          [:div.player.window.vstack {:class (when detail-visible? :detail-visible)}
           [:div.detail {:class-name (:type item)} ;'audio' or 'video'
            [minimise-button state "x" :detail-visible?]
            [:div.player-container
             [media-player state]
             (when audio?
               [:div.album-art
                [:img {:src (paths/l-image-path (:id item))}]])]
            [detail/detail-component state]]
           (when detail-visible?
             [:div.toolbar
              [toolbar-item "INFO"]
              [toolbar-item "$"]
              [toolbar-item "FULLSCREEN" (fn []
                                           (js/console.log "Inca nu-i gata!"))]])
           [:div.content
            [min-player state]]]
          [:div.player.window.hidden])))))