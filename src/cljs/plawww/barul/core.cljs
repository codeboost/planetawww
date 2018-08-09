(ns plawww.barul.core
  (:require
   [goog.net.XhrIo]
   [cljs.reader]
   [cljs.core.async :as async :refer [<! >! chan close!]]

   [reagent.core :as r])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def actors
  [["Vanea"
    [[0 1.289]
     [2.461 4.040]
     [4.574 6.548]]]

   ["Jora"
    [[7.628 10.426]
     [11.157 14.848]
     [15.871 17.113]]]

   ["Serioja"
    [[19.225 22.590]
     [24.345 27.645]
     [29.544 31.613]
     [33.551 36.878]
     [38.633 41.173]
     [42.915 46.804]]]])

(defn random-actor-slice [i]
  (let [slices (second (nth actors i))]
    (if-not slices
      [0 0]
      (nth slices (rand-int (count slices))))))

(defn decode-audio-data
  [context data]
  (let [ch (chan)]
    (.decodeAudioData context
                      data
                      (fn [buffer]
                        (go (>! ch buffer)
                            (close! ch))))
    ch))

(defn get-url [url response-type]
  (let [ch (chan)]
    (doto (goog.net.XhrIo.)
      (.setResponseType response-type)
      (.addEventListener goog.net.EventType.COMPLETE
                         (fn [event]
                           (let [res (-> event .-target .getResponse)]
                             (go (>! ch res)
                                 (close! ch)))))
      (.send url "GET"))
    ch))

(comment
  (go
    (let [src (<! (get-url "/data/sounds/barul-2.edn" "text"))
          actors (cljs.reader/read-string src)])))



(defn url->buffer [context url on-ready]
  (go
   (let [response (<! (get-url (str url ".mp3") "arraybuffer"))
         buffer (<! (decode-audio-data context response))
         actors-text (<! (get-url (str url ".edn") "text"))
         actors (cljs.reader/read-string actors-text)]
     (on-ready {:buffer buffer
                :actors actors}))))

(defn buffer->source [context buffer]
  (let [source (.createBufferSource context)]
    (aset source "buffer" buffer)
    source))

(def audio-context (atom nil))

(defn reset-audio-context! []
  (when @audio-context
    (.close @audio-context)))

(defn create-audio-context! []
  (reset-audio-context!)

  (let [AudioContext (or (.-AudioContext js/window)
                         (.-webkitAudioContext js/window))]
    (reset! audio-context (AudioContext.))
    @audio-context))

(def multi-track (r/atom true))

(def playing-sources (atom #{}))

(defn stop-all! []
  (doseq [source @playing-sources]
    (.stop source))
  (reset! playing-sources #{}))

(defn play-slice [buffer slice]
  (let [source (buffer->source @audio-context buffer)
        _ (.connect source (.-destination @audio-context))
        [start-time end-time] slice
        duration (- end-time start-time)]
    (js/console.log "Playing slice at " start-time " for " duration "seconds on source " source)
    (if-not @multi-track
      (do
        (stop-all!)
        (reset! playing-sources #{source}))
      (swap! playing-sources conj source))
    (.start source 0 start-time duration)))


(defn bar-page [buffer]
  [:div.barul-page
   [:h3 "Barul"]
   [:div
    [:input {:type :checkbox
             :checked @multi-track
             :on-change (fn [e]
                          (reset! multi-track (.. e -target -checked)))}]
    [:span " Multi-track?"]]
   [:div.ab.person
    {:on-click #(play-slice buffer (random-actor-slice 0))}
    "VANEA"]
   [:div.ab.person
    {:on-click #(play-slice buffer (random-actor-slice 1))}
    "LIONEA"]
   [:div.ab.person
    {:on-click #(play-slice buffer (random-actor-slice 2))}
    "SERIOJA"]
   [:div.ab.stop-button
    {:on-click #(stop-all!)}
    "STOP"]])


(defn page []
  (let [url-base "/data/sounds/barul-2"
        buffer (r/atom nil)]
    (r/create-class
     {:component-did-mount
      (fn []
        (let [context (create-audio-context!)]
          (when context
            (url->buffer context url-base #(reset! buffer %)))))

      :component-will-unmount
      (fn [] (reset-audio-context!))

      :reagent-render
      (fn []
        (if-not @buffer
          [:div "Incarcare..."]
          [bar-page @buffer]))})))


