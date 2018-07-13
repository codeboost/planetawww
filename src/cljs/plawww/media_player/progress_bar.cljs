(ns plawww.media-player.progress-bar
  (:require
    [reagent.core :as r]
    [reagent.interop :refer-macros [$ $!]]))


(defn percent-width [object x]
  (let [width ($ object width)]
    (cond (pos? width) (/ x width)
          :else 0)))

(defn percent-height [object y]
  (let [height ($ object height)]
    (cond (pos? height) (/ y height)
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
      (let [percent (* 100 (min 1 progress))]
        {:style {:width (str percent "%")
                 :padding-left (if (and (> percent 0) (< percent 10)) "8px" "0px")}})]])


(defn vertical-progress-bar [progress callback]
  [:div.vertical-progress-bar
   {:on-click
    (fn [e]
      (let [_this (r/current-component)
            target (js/$ ($ e :target))
            faker  (.find target ".faker")
            faker-height (if faker ($ faker height) 0)
            page-y ($ e :pageY)
            offset ($ target offset)
            offset-top ($ offset :top)
            offset-y (- page-y offset-top)
            offset-y (- ($ target height) offset-y faker-height)
            offset-y (if (<= offset-y 8) 0 offset-y) ;trim to 0 if clicked within 8 pixels.
            percent (percent-height target offset-y)]
        (callback percent)))}
   [:div.vertical-progress-bar-progress
    (let [percent (* 100 (min 1 progress))]
      {:style {:height (str percent "%")}})]
   [:div.faker]])