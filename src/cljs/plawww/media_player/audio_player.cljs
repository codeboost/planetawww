;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.media-player.audio-player
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
            [cljs.core.async :refer (chan put! <!)]
            [reagent.session :as session]
            [reagent.interop :refer-macros [$ $!]]))

;It is assumed that the soundManager 2 library is included in the page.
;This implementation has been refactored several times as I was getting more experienced with the language.
;Right now it's a bit of a mess, so a total re-write might be in order at some point.

(defn logv [& args]
  (println "audio-player:" (apply str args)))

(defn- set-playback-state
  "Updates the session state, by setting :player-state :playback-state to the supplied value `s`."
  [s]
  (logv "set-playback-state: " s)
  (session/update-in! [:player-state] assoc :playback-state s))

(defn- set-playback-position
  "Updates the session state. Sets the position in :player-state :position field."
  [pos]
  (session/update-in! [:player-state] assoc :position pos))

(defonce *ctrl-channel (atom nil))

(defn- save-control-channel! [channel]
  (reset! *ctrl-channel channel))

;FIXME: Hack
(defn- amp-volume
  "Converts to logarithmic value."
  [percent]
  (let [amp (Math/pow percent 4)]
    (* amp 100)))

;FIXME: Check docstring
(defn- session-amp-volume
  "Returns the current session's volume converted to logarithmic volume."
  []
  (amp-volume (session/get-in [:player-state :volume])))

(defn- while-playing
  "SMSound whileplaying callback.
  Calls set-playback-position to notify the world of playback progress."
  []
  (this-as this
    (let [position (.-position this)
          duration (.-duration this)]
      (when (pos? duration)
        (set-playback-position (/ position duration))))))

(defonce audio-url  (atom nil)) ;audio file path
(defonce *current-sound (atom nil))

(defn- try-create-sound!
  "Creates a sound object and sets the url to the supplied path.
       soundManager.createSound({url: '...'});"
  [& [url]]
  (reset! *current-sound nil)
  (if-let [url (or url @audio-url)]
    (try
      (let [_ (logv "audio-player: try-create-sound! " url)
            s (.createSound
                js/soundManager
                (clj->js
                  {:url      url
                   :whileplaying while-playing
                   :onfinish #(set-playback-state :stop)
                   :onstop   #(set-playback-state :stop)
                   :onplay   #(set-playback-state :play)
                   :onpause  #(set-playback-state :pause)
                   :onresume #(set-playback-state :play)
                   :onerror  (fn [& args]
                               (logv "soundManager: onerror: " args))}))]
        (when s (.setVolume s (session-amp-volume)))
        (reset! *current-sound s))
      (catch js/Object e
        (logv "Exception thrown when creating sound player: " e)))
    (logv "audio-player: try-create-sound! called with empty url, ignored.")))


;This is the dispatch function for the commands received on the control channel.
(defmulti exec-cmd (fn [_ {:keys [command]}] command))

(defmethod exec-cmd :play [s]
  (logv "play")
  (if ($ s :paused)
    ($ s resume)
    ($ s play)))

(defmethod exec-cmd :stop [s]
  (logv "stop")
  (reset! @audio-url nil)
  ($ s stop))

(defmethod exec-cmd :pause [s]
  (logv "pause")
  ($ s pause))

(defmethod exec-cmd :set-pos [s {:keys [percent]}]
  (logv "setPos: " percent)
  (let [duration ($ s :duration)]
    ($ s setPosition (* duration percent))))

(defmethod exec-cmd :set-volume [s {:keys [percent]}]
  (logv "setVolume: " percent)
  (.setVolume s (session-amp-volume)))

(defn- load-audio-file [cmd]
  (when @*current-sound ($ @*current-sound stop))
  (reset! audio-url (:filename cmd))
  (try-create-sound!)
  (when (and (:should-play cmd) @*current-sound)
    (exec-cmd @*current-sound {:command :play})))

(defn- process-command
  "Dispatches a command to the player.
  `s` is an atom which is reset to a new value when the :load command received."
  [cmd]
  (let [cmd (if (keyword? cmd) {:command cmd} cmd)]
    (if (= :load (:command cmd))
      (load-audio-file cmd)
      (do
        (when-not @*current-sound
          ;Can happen on first load.
          ;The load command could not create the audio player because user hasn't interacted with the page
          ;try-create-sound! will use @audio-url global (if it is set) to load the current audio file.
          (logv "audio-player: recreating sound.")
          (try-create-sound!))
        (when @*current-sound
         (exec-cmd @*current-sound cmd))))))

(defn init
  "Creates a command channel and returns it.
  The channel is used to send commands to the player.
  {:command :play}
  "
  []
  (let [ctrl-chan (chan)
        _ (save-control-channel! ctrl-chan)]
    (js/console.log "audio-player/init")
    (go-loop []
      (when-let [cmd (<! ctrl-chan)]
        (logv "processing command: " cmd)
        (process-command cmd)
        (recur)))))

(defn command [cmd]
  (if-let [channel @*ctrl-channel]
    (put! channel cmd)
    (logv "Cannot process command. Audio player not initialised.")))