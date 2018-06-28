(ns plawww.media-player.controller
  (:require
   [cljs.core.async :refer [put!]]
   [reagent.core :as reagent]
   [reagent.session :as session]
   [plawww.paths :as paths]
   [plawww.media-player.audio-player :as audio-player]
   [plawww.medialist.core :as media-page]))

(def default-player-state {:visible false
                           :detail-visible? false
                           :position 0
                           :volume 0.6
                           :should-show-detail? true
                           :item {:title ""
                                  :duration 0}})

(defn start-playback [item]
  (js/console.log "start-playback: " (:filename item))
  (if-let [filename (:filename item)]
    (audio-player/command {:command :load
                           :filename (paths/media-path filename)
                           :should-play true})
    (js/console.log "start-playback: no filename for item " item)))

(defn hook-up-the-stuff
  []
  (js/console.log "hool-up-the-stuff")
  (audio-player/init)
  (session/put! :player-state default-player-state))

(defn with-item-image [item]
  (assoc item :image (paths/s-image-path (:id item))))

(defn set-current-media-item
  "Updates the current media item in the session-state, which should trigger the media player to show and load the corresponding media.
  If :should-show-detail? is true, detail-visible? is set to true (which opens the player in detail mode).
  This only happens once, if user decides to hide details, the player will stay closed when a new item is loaded.
  This way, the user gets control of the detail showing, allowing him to toggle it.
  "
  [item]
  (let [item    (with-item-image item)
        state   (or (session/get :player-state) default-player-state)
        detail? (or (:should-show-detail? state) (:detail-visible? state))]
    (media-page/set-opts {:selected-id (:id item)})
    (session/update-in! [:player-state] merge {:item item :detail-visible? detail? :visible true})
    (when detail? (session/update-in! [:player-state] dissoc :should-show-detail?))
    (start-playback item)))