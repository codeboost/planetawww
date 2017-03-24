(ns plawww.crt
  (:require [plawww.media-player :as player]))

(defn crt-page [content]
  [:div.vert-container
   [:div.tv.noisy
    [:div.frame.tv
     [:div.piece.output content]]
    [player/player]]
   ])
