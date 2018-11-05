(ns plawww.media-player.oscilloscope
  (:require [reagent.core :as r]))


(defn get-2d-context [canvasElement]
  (let [context (.getContext canvasElement "2d")]
    (aset context "strokeStyle" "#14fdce")
    (aset context "lineWidth" 1)
    (aset context "shadowColor" "#14fdce")
    context))

(def state (r/atom nil))

(defn- create-audio-context []
  (when-not (:audio-context @state)
    (let [AudioContextConstructor (or js/window.AudioContext
                                      js/window.webkitAudioContext
                                      js/window.mozAudioContext
                                      js/window.oAudioContext
                                      js/window.msAudioContext)
          audioContext (new AudioContextConstructor)]
      (swap! state assoc :audio-context audioContext)))
  (:audio-context @state))

(defn- create-media-element-source [audio-context audio-element]
  (let [destination (.-destination audio-context)
        source (or (:source @state) (.createMediaElementSource audio-context audio-element))]
    (swap! state assoc :source source)
    (.connect source destination)
    source))

(defn destroy-oscilloscope []
  (let [{:keys [source audio-context]} @state]
    (when source
      (.disconnect source (.-destination (:audio-context @state))))
    (when audio-context
      (.close audio-context))))

(defn resume-context []
  (js/console.log "resume-context: " (:audio-context @state))
  (when (:audio-context @state)
    (.resume (:audio-context @state))
    (js/console.log "context state after resume:" (.-state (:audio-context @state)))))

(defn create-oscilloscope [canvas-element audio-element]
  (let [audio-context (create-audio-context)
        source (create-media-element-source audio-context audio-element)
        scope (new js/window.Oscilloscope source)
        draw-context (get-2d-context canvas-element)]
    #_(js/console.log "scope: " scope "; context:" audio-context "; context state:" (.-state audio-context))
    (if (and scope draw-context)
      (do
        (.animate scope draw-context)
        (.resume audio-context))
      (js/console.error "create-oscilloscope error: scope or context is nil: " scope draw-context))))

