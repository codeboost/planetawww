(ns plawww.media-player.controller
  (:require
   [cljs.core.async :refer [put!]]
   [reagent.core :as reagent]
   [reagent.session :as session]
   [plawww.paths :as paths]
   [plawww.media-item.core :as media-item]
   [plawww.medialist.explorer :as explorer]
   [plawww.medialist.core :as media-page]))

(def default-player-state {:visible false
                           :detail-visible? false
                           :should-show-detail? true})

(defn with-item-image [item]
  (assoc item :image (paths/s-image-path (:id item))))

(defn start-playback [item])

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
    (explorer/show-detail item)))

    ;(media-player/set-current-item item)))
