(ns plawww.media-player.fullscreen
  (:require [reagent.core :as r]
            [screenfull]))


(def hide-controls-timer (atom 0))

(defn stop-hide-controls-timer! []
  (js/clearTimeout @hide-controls-timer)
  (reset! hide-controls-timer 0))

(defn hide-controls-after-a-while [state]
  (stop-hide-controls-timer!)
  (reset!
   hide-controls-timer
   (js/setTimeout #(do
                     (when (:fullscreen? @state)
                       (swap! state assoc :fullscreen-controls? false)))
                  3000)))

(defn show-fullscreen-controls! [state]
  (stop-hide-controls-timer!)
  (swap! state assoc :fullscreen-controls? true))

(defn show-then-hide-fullscreen-controls! [state]
  (when (:fullscreen? @state)
    (swap! state assoc :fullscreen-controls? true)
    (hide-controls-after-a-while state)))

(defn toggle-fn
  "Returns a function, which, when called, toggles `el` into/out of fullscreen mode.
  If `screenfull` is supported, calls `screenfull.toggle()`.
  When in fullscreen mode, the controls will become hidden (:fullscreen-controls? will be set to false) after a while.
  If screenfull is not supported (eg. on iOS Safari), the fullscreen controls will immediately be hidden.
  To restore controls, call `show-fullscreen-controls!` from the mousemove or touch handlers.
  "

  [state el]
  (if js/screenfull
    (.on js/screenfull "change" #(let [fullscreen? (.-isFullscreen js/screenfull)]
                                   (swap! state assoc :fullscreen? fullscreen?)
                                   (when fullscreen?
                                     (hide-controls-after-a-while state)))))
  (fn []
    (if js/screenfull
      (.toggle js/screenfull el)
      (do
        (swap! state update :fullscreen? not)
        (if (:fullscreen? @state)
          (swap! state assoc :fullscreen-controls? false))))))

