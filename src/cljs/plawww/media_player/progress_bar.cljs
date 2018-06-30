(ns plawww.media-player.progress-bar
  (:require
    [reagent.core :as r]
    [reagent.interop :refer-macros [$ $!]]))


(defn percent-width [object x]
  (let [width ($ object width)]
    (cond (pos? width) (/ x width)
          :else 0)))


(defn progress-bar [progress callback]
    [:div.progress-bar {:on-click (fn [e]
                                    (let [_this (r/current-component)
                                          target (js/$ ($ e :target))
                                          pagex ($ e :pageX)
                                          offset ($ target offset)
                                          offsetLeft ($ offset :left)
                                          offsetx (- pagex offsetLeft)
                                          offsetx (if (<= offsetx 8) 0 offsetx) ;trim to 0 if clicked within 8 pixels.
                                          percent (percent-width target offsetx)]
                                      (callback percent)))}
     [:div.progress-bar-progress
      (let [percent (* 100 (min 1 progress))
            _ (println "percent=" percent)]
        {:style {:width (str percent "%")
                 :padding-left (if (and (> percent 0) (< percent 10)) "8px" "0px")}})]])
