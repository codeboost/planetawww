(ns plawww.media-player.oscilloscope
  (:require [reagent.core :as r]))


(defn get-2d-context [canvasElement]
  (let [context (.getContext canvasElement "2d")]
    (aset context "strokeStyle" "#14fdce")
    (aset context "lineWidth" 1)
    (aset context "shadowColor" "#14fdce")
    context))


(def oscilloscope-source (atom nil))

(defn- create-audio-context []
  (let [AudioContextConstructor (or js/window.AudioContext
                                    js/window.webkitAudioContext
                                    js/window.mozAudioContext
                                    js/window.oAudioContext
                                    js/window.msAudioContext)
        audioContext (new AudioContextConstructor)]
    audioContext))

(defn create-media-element-source [audio-context audio-element]
  (let [destination (.-destination audio-context)
        source (.createMediaElementSource audio-context audio-element)]
    (.connect source destination)
    source))

(defn create-oscilloscope [canvas-element audio-element]
  (let [audio-context (create-audio-context)
        source (create-media-element-source audio-context audio-element)
        scope (new js/window.Oscilloscope source)
        draw-context (get-2d-context canvas-element)]
    (reset! oscilloscope-source source)
    (if (and scope draw-context)
      (.animate scope draw-context)
      (js/console.error "create-oscilloscope error: scope or context is nil: " scope draw-context))))

(defn stop-oscilloscope []
  (js/console.log "stop-oscilloscope")
  (let [source @oscilloscope-source]
    (reset! oscilloscope-source nil)
    (when source (.stop source))))