(ns plawww.audio-player
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.core.async :refer (chan put! <!)]
            [reagent.session :as session]))

;The one and only current sound
(defonce gCurrentSound (reagent/atom nil))

(defn- set-playback-state [s]
  (session/update-in! [:player-state] assoc :playback-state s))

(defn- create-sound
  "Creates a sound object and sets the url to the supplied path.
       soundManager.createSound({url: '...'});"
  [p]
  (.createSound js/soundManager (clj->js {:url p})))

(defn play
  "Calls the .play() method of the `sound`.
  Uses gCurrentSound if called without arguments."
  ([s]
   (when s
     (.play s)
     (set-playback-state :play)))
  ([]
   (play @gCurrentSound)))

(defn stop
  "Calls .stop() method on `sound`.
  Uses `gCurrentSound` if called without arguments."
  ([s]
   (when s
     (.stop s)
     (set-playback-state :stop)))
  ([]
   (stop @gCurrentSound)))

(defn load [p]
  (stop)
  (when-let [s (create-sound p)]
    (reset! gCurrentSound s)))

(defn load-and-play
  "Loads a sound file and plays it."
  [p]
  (when (load p) (play)))

(defmulti exec-cmd :command)
(defmethod exec-cmd :play [] (play))
(defmethod exec-cmd :stop [] (stop))
(defmethod exec-cmd :load [{:keys [filename should-play]}]
  (print "Loading " filename)
  (load filename)
  (when should-play (play)))

;  (session/put! :audio-player-control-channel gEventChannel)

(defn init
  []
  (let [ctrl-channel (chan)]
    (go-loop []
             (when-let [command (<! ctrl-channel)]
               (cond
                 (keyword? command) (exec-cmd {:command command})
                 :else (exec-cmd command (:args command)))
               (recur)))
    ctrl-channel))

;--------
(comment

  (def test-map {:one 1
                 :two 2
                 :player-state {
                                :playback-state :none
                                }})

  test-map
  (let [r (update-in test-map [:player-state] assoc :playback-state :play)] r)

  (exec-cmd {:command :load
             :args ["hello"]})

  ;Here's how to track the atom.
  ;In this example, whenever gCurrentSound changes, our function is called.
  ;If you deref an atom in your function, then your function is called when atom changes.
  (let [trackable (reagent/track! (fn[]
                   (print "Current sound is " @gCurrentSound)))]
    trackable)


  ;Here's how to use `this` in javascript.
  (defn testing []
    (this-as this
      (print this)))

  (testing)

  ;load media file and start playing
  (load-and-play "/data/media/293-vama.mp3")
  (stop)
  )