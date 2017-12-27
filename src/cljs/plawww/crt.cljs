(ns plawww.crt
  (:require [plawww.media-player.core :as player]))

(defn crt-page [content]
  [:div.vert-container
   [:div.tv.noisy
    [:div.frame.tv
     [:div.piece.output content]]
    [player/player]]])


