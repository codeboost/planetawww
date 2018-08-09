(ns plawww.barul.core
  (:require
   [goog.net.XhrIo]
   [cljs.reader]
   [cljs.core.async :as async :refer [<! >! chan close!]]
   [reagent.core :as r])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def state (r/atom {:audio-context nil
                    :multi-track? false
                    :playing-sources #{}}))

(defn random-actor-slice [actors i]
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

(defn reset-audio-context! []
  (when (:audio-context @state)
    (.close (:audio-context @state))))

(defn create-audio-context! []
  (reset-audio-context!)
  (let [AudioContext (or (.-AudioContext js/window)
                         (.-webkitAudioContext js/window))]
    (swap! state assoc :audio-context (AudioContext.))
    (:audio-context @state)))

(defn stop-all! []
  (doseq [source (:playing-sources @state)]
    (.stop source))
  (swap! state assoc :playing-sources #{}))

(defn play-slice [buffer slice]
  (let [source (buffer->source (:audio-context @state) buffer)
        _ (.connect source (.-destination (:audio-context @state)))
        [start-time end-time] slice
        duration (- end-time start-time)]
    (js/console.log "Playing slice at " start-time " for " duration "seconds on source " source)
    (if-not (:multi-track? @state)
      (do
        (stop-all!)
        (swap! state assoc :playing-sources #{source}))
      (swap! state update-in [:playing-sources] conj source))
    (.start source 0 start-time duration)))

(defn bar-page [{:keys [buffer actors] :as stage}]
  (js/console.log "buffer: " buffer "actors: " actors)
  [:div.barul-page
   [:h3 "Barul"]
   [:div
    [:input {:type :checkbox
             :checked (:multi-track? @state)
             :on-change #(swap! state assoc :multi-track? (.. % -target -checked))}]
    [:span " Multi-track?"]]
   [:div.ab.person
    {:on-click #(play-slice buffer (random-actor-slice actors 0))}
    "VANEA"]
   [:div.ab.person
    {:on-click #(play-slice buffer (random-actor-slice actors 1))}
    "LIONEA"]
   [:div.ab.person
    {:on-click #(play-slice buffer (random-actor-slice actors 2))}
    "SERIOJA"]
   [:div.ab.stop-button
    {:on-click #(stop-all!)}
    "STOP"]])

(defn page []
  (let [url-base "/data/sounds/barul-2"
        stage (r/atom nil)]
    (r/create-class
     {:component-did-mount
      (fn []
        (let [context (create-audio-context!)]
          (when context
            (url->buffer context url-base #(reset! stage %)))))

      :component-will-unmount
      (fn [] (reset-audio-context!))

      :reagent-render
      (fn []
        (if-not @stage
          [:div "Incarcare..."]
          [bar-page @stage]))})))


