(ns plawww.media-player.oscilloscope
  (:require [reagent.core :as r]))

(def oscilloscope-enabled? false)

(defn get-2d-context [canvasElement]
  (let [context (.getContext canvasElement "2d")]
    (aset context "strokeStyle" "#14fdce")
    (aset context "lineWidth" 1)
    (aset context "shadowColor" "#14fdce")
    context))

(def state (r/atom {:audio-context nil
                    :source nil
                    :element nil
                    :scope nil
                    :draw-context nil
                    :anim-type :none}))

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
        source (or (and (= audio-element (:element @state)) (:source @state))
                   (.createMediaElementSource audio-context audio-element))]
    (swap! state assoc :source source :element audio-element)
    (.connect source destination)
    source))

(defn- destroy-oscilloscope []
  (let [{:keys [source audio-context]} @state]
    (when source
      (.disconnect source (.-destination (:audio-context @state))))
    (when audio-context
      (.close audio-context))
    (reset! state {:audio-context nil :source nil})))

(defn- stop-animation [audio-context scope]
  (.then (.suspend audio-context)
    #(.stop scope)))

(defn- resume-animation [audio-context draw-context scope]
  (.animate scope draw-context)
  (.resume audio-context))

(defn set-oscilloscope-type [type]
  (when oscilloscope-enabled?
    (let [{:keys [scope audio-context draw-context anim-type]} @state]
      (when (not= anim-type type)
        (when scope
          (.setDrawfn scope (name type))
          (if (= :none type)
            (stop-animation audio-context scope)
            (resume-animation audio-context draw-context scope)))
        (swap! state assoc anim-type type)))))

(defn create-oscilloscope [canvas-element audio-element]
  (when (and oscilloscope-enabled? (= js/HTMLAudioElement (type audio-element)))
    (let [audio-context (create-audio-context)
          source (create-media-element-source audio-context audio-element)
          scope (new js/window.Oscilloscope source)
          draw-context (get-2d-context canvas-element)]
      (if (and scope draw-context)
        (swap! state assoc :scope scope :draw-context draw-context)
        (js/console.error "create-oscilloscope error: scope or context is nil: " scope draw-context)))))

