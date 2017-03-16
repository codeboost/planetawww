(ns plawww.crt)

(defn crt-page [content]
  [:div.vert-container
   [:div.tv.noisy
    [:div.frame.tv
     [:div.piece.output content]]]])
