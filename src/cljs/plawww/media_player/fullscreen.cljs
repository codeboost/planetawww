(ns plawww.media-player.fullscreen
  (:require [screenfull]))


(def hide-controls-timer (atom 0))

(defn stop-hide-controls-timer! []
  (js/clearTimeout @hide-controls-timer)
  (reset! hide-controls-timer 0))

(defn hide-controls-after-a-while! [state]
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
    (hide-controls-after-a-while! state)))

(defn restore-fullscreen!
  "Exits fullscreen mode, if currently active."
  [state]
  (if js/screenfull
    (when (.-isFullscreen js/screenfull)
      (.exit js/screenfull))
    (do
      (stop-hide-controls-timer!)
      (swap! state assoc
             :fullscreen? false
             :fullscreen-controls? true))))

(defn register-change-listener!
  "Registers a screenfull 'change' listener which syncs :fullscreen? in `state`
  and schedules hiding the controls when entering fullscreen.
  Returns the handler, to be passed to `unregister-change-listener!` on teardown.
  Call once per player instance - registering on every render leaks listeners."
  [state]
  (when js/screenfull
    (let [handler! (fn []
                     (let [fullscreen? (.-isFullscreen js/screenfull)]
                       (swap! state assoc :fullscreen? fullscreen?)
                       (if fullscreen?
                         (hide-controls-after-a-while! state)
                         (show-fullscreen-controls! state))))]
      (.on js/screenfull "change" handler!)
      handler!)))

(defn unregister-change-listener! [handler!]
  (when (and js/screenfull handler!)
    (.off js/screenfull "change" handler!)))

(defn toggle!
  "Toggles `el` into/out of fullscreen mode.
  If `screenfull` is supported, calls `screenfull.toggle()` and the change
  listener keeps `state` in sync. Otherwise (eg. iOS Safari), tracks the
  fullscreen state manually. CSS expands the player to the viewport and the
  mouse/touch handlers keep the controls reachable."
  [state el]
  (if js/screenfull
    (.toggle js/screenfull el)
    (let [entering? (not (:fullscreen? @state))]
      (swap! state assoc
             :fullscreen? entering?
             :fullscreen-controls? true)
      (if entering?
        (hide-controls-after-a-while! state)
        (stop-hide-controls-timer!)))))
