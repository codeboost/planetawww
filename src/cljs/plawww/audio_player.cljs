;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns plawww.audio-player
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.core.async :refer (chan put! <!)]
            [reagent.session :as session]
            [reagent.interop :refer-macros [$ $!]]))

;It is assumed that the soundManager 2 library is included in the page.

(defn- set-playback-state
  "Updates the session state, by setting :player-state :playback-state to the supplied value `s`."
  [s]
  (session/update-in! [:player-state] assoc :playback-state s))

(defn- set-playback-position
  "Updates the session state. Sets the position in :player-state :position field."
  [pos]
  (session/update-in! [:player-state] assoc :position pos))

(defn play
  "Calls the .play() method of the `sound`."
  ([s]
   (when s
     (.play s))))

(defn stop
  "Calls .stop() method on `sound`."
  ([s]
   (when s
     (.stop s))))

(defn pause
  "Calls .pause() method on `sound`."
  [s]
  (when s
    (.pause s)))

(defn- while-playing
  "SMSound whileplaying callback.
  Calls set-playback-position to notify the world of playback progress."
  []
  (this-as this
    (let [position (.-position this)
          duration (.-duration this)]
      (when (pos? duration)
        (set-playback-position (/ position duration))))))

(defn- create-sound
  "Creates a sound object and sets the url to the supplied path.
       soundManager.createSound({url: '...'});"
  [p]
  (let [s (.createSound
            js/soundManager
            (clj->js
              {:url      p
               :whileplaying while-playing
               :onfinish (fn [] (set-playback-state :stop))
               :onstop   (fn [] (set-playback-state :stop))
               :onplay   (fn [] (set-playback-state :play))}))]
    s))

(defn load
  "Loads sound file at path `p`."
  [s p]
  (when s (stop s))
  (create-sound p))

;This is the dispatch function for the commands received on the control channel.
(defmulti exec-cmd (fn [s {:keys [command]}] command))

(defmethod exec-cmd :play [s] (play s))

(defmethod exec-cmd :stop [s] (stop s))

(defmethod exec-cmd :pause [s] (pause s))

(defmethod exec-cmd :load [s {:keys [filename should-play]}]
  (let [new-s (load s filename)]
    (when should-play (play new-s))
    new-s))

(defmethod exec-cmd :set-pos [s {:keys [percent]}]
  (let [duration ($ s :duration)]
    ($ s setPosition (* duration percent))))


(defn process-command
  "Dispatches a command to the player.
  `s` is an atom which is reset to a new value when the :load command received."
  [s cmd]
  (if (keyword? cmd)
    (exec-cmd @s {:command cmd})
    (let [res (exec-cmd @s cmd)]
      (when (= :load (:command cmd)
               (do
                 (reset! s res)))))))

(defn init
  "Creates a command channel and returns it.
  The channel is used to send commands to the player.
  {:command :play}
  "
  []
  (let [ctrl-channel (chan)
        s (reagent/atom nil)]
    (go-loop []
             (when-let [cmd (<! ctrl-channel)]
               (process-command s cmd)
               (recur)))
    ctrl-channel))
