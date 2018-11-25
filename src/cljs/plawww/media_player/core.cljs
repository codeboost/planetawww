;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.media-player.core
  (:require
   [cljsjs.react-player]
   [plawww.components.components :refer [minimise-button tag-list-component]]
   [plawww.media-item.media-item :as media-item]
   [plawww.media-player.item-detail :as detail]
   [plawww.media-player.progress-bar :as progress-bar]
   [plawww.utils :as utils]
   [reagent.interop :refer-macros [$ $!]]
   [plawww.paths :as paths]
   [reagent.core :as r]
   [reagent.session :as session]
   [plawww.mediadb.core :as db]
   [plawww.ui :as ui]))



(def react-player (r/adapt-react-class js/ReactPlayer))

(defonce mplayer (r/atom nil))
(def canvas-el (r/atom nil))
(defonce the-player (r/atom nil))


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
                                :fullscreen? false
                                :share-dialog-visible? false}))



(defn s->ms
  "Seconds to milliseconds."
  [s]
  (* 1000 s))

(defn ms->s
  "Milliseconds to seconds"
  [s]
  (/ s 1000))

(defn set-current-item [item]
  (let [muted (:muted @mplayer-state)
        playing? (not muted)
        detail-visible? (or (= (:type item) "video")
                            (not (:playing @mplayer-state)))]
    (swap! mplayer-state merge {:item item
                                :visible true
                                :detail-visible? true
                                :playing playing?})
    (utils/ga "set" "page" (.-pathname (.-location js/window)))
    (utils/ga "send" "pageview"))
  (reagent.core/flush))

(defn is-playing? []
  (:playing @mplayer-state))

(defn set-detail-visible [visible?]
  (swap! mplayer-state assoc :detail-visible? visible? :volume-visible? false))

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

(defn toggle-play [state]
  (let [{:keys [muted]} @state]
    (if muted ; Safari restriction - media must be loaded in a muted state.
      (flush-play! state)
      (swap! state update :playing not))))

(defn play-button
  "Play button component."
  []
  (fn [state]
    (let [{:keys [playing]} @state
          text (if playing "||" ">>")]
      [:div.accessory-button.play-button
       {:class (when playing :selected)
        :on-click #(toggle-play state)}
       text])))


(defn song-progress [played on-click]
  [:span.song-progress
   (progress-bar/progress-bar played on-click)])

(defn- toggle-accessory-button
  [state text key]
  [:div.accessory-button
   {:on-click #(swap! state update-in [key] not)
    :class    (when (@state key) :selected)}
   text])

(defn- update-played-time! [mplayer state]
  (let [current-time (.getCurrentTime @mplayer)
        duration     (.getDuration @mplayer)
        played       (if (pos? duration) (/ current-time duration) 0)]
      (swap! state merge {:played played :duration duration})))


(defn- start-update-duration-timer
  "update-interval is an atom which will contain the interval id.
   state is the atom where the :duration and :played values will be stored."
  [mplayer state update-interval]
  (reset!
   update-interval
   (js/setInterval
    #(let [playing      (:playing @state)]
       (when playing
         (update-played-time! mplayer state)))
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
                           (swap! state assoc :playing true)))
             :on-ended #(swap! state assoc :playing false)
             :on-ready (fn [])
             :on-play  #(swap! state assoc :playing true)
             :on-pause #(swap! state assoc :playing false)
             :on-error (fn [err]
                         (swap! state merge {:playing false :error err})
                         (js/console.log "Error: " err))
             :on-duration #(swap! state assoc :duration %)
             :on-progress (fn [p]
                            ;don't do this - scrolling won't work
                            ;there's a timer that takes care of it
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
  (let [ls state]
    (fn []
      (let [{:keys [volume]} @state]
        [:div.volume-control
         [toggle-accessory-button ls (volume-text volume) :volume-visible?]
         (when (:volume-visible? @ls)
           [:div.volume-selector
            [progress-bar/vertical-progress-bar
             volume
             #(swap! state assoc :volume %)]
            [:div.faker]])]))))

(defn medium-player [state]
  [:div.min-player
   [:div.controls
    [play-button state]
    [song-progress (:played @state) #(do
                                       (.seekTo @mplayer %)
                                       (update-played-time! mplayer state))]
    [toggle-accessory-button state "i" :detail-visible?]
    [volume-control state]]])

(defn toolbar-item [title on-click]
  [:div.toolbar-item
   {:on-click on-click}
   title])

(defn player-toolbar [state]
  (fn []
    (let [item (:item @state)
          video? (= "video" (get-in @state [:item :type]))]
      [:div.toolbar
       [toolbar-item "INFO" (fn []
                              (swap! state assoc :detail-visible? false)
                              (session/put! :current-media-item item))]
       [toolbar-item [detail/duration-comp @state]]
       [toolbar-item "SHARE" #(swap! state assoc :share-dialog-visible? true)]
       (when video?
         [toolbar-item "FULLSCREEN" #(.request js/window.screenfull (r/dom-node @the-player))])])))

(defn player []
  (let [state mplayer-state]
    (fn []
      (let [{:keys [visible item detail-visible? share-dialog-visible? type]} @state]
        (if visible
          [:div.player.window.vstack {:class (when detail-visible? "detail-visible")
                                      :ref #(reset! the-player %)}
           [:div.detail {:class (:type item)}
            [media-item/info-component item {:show-details? (= (:type item) "audio")}]
            [media-player state]
            [minimise-button "x" #(set-detail-visible false)]]
           (when detail-visible?
             [player-toolbar state])
           (when share-dialog-visible?
             [ui/share-dialog-modal {:on-close #(swap! state assoc :share-dialog-visible? false)
                                     :share-url (.-href (.-location js/window))}])
           [:div.content
            [medium-player state]]]
          [:div.player.window.hidden])))))