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



(defn clamp-v
  "Rounds off `v` to 0 if `v < vmin` and to 1 if `v > vmax`.
  v is a double value in the range 0..1"
  [v vmin vmax]
  (cond
    (< v vmin) 0
    (> v vmax) 1
    :else v))

(defn vertical-progress-bar [progress callback]
  (let [el (r/atom nil)]
    [:div.vertical-progress-bar
     {:ref #(reset! el %)
      :on-click
      (fn [e]
        (let [_this (r/current-component)
              _ (js/console.log "_this:" el)
              target (js/$ ($ e :target))
              ;faker  (.find target ".faker")
              ;progress-el (.find target ".vertical-progress-bar-progress")
              ;progress-bottom (bottom-offset (.get progress-el 0) (.get target 0))
              ;faker-height (if faker ($ faker height) 0)
              height ($ target height)
              _ (js/console.log "height = " height)
              bottom (+ height (.. ($ target offset) -top))
              click-y ($ e :pageY)
              click-pos (- bottom click-y)
              percent (if (pos? height) (/ click-pos height) 0)
              percent (clamp-v percent 0.1 0.95)
              _ (js/console.log "percent=" percent)]
          (callback percent)))}
     [:div.vertical-progress-bar-progress
      (let [percent (* 100 (min 1 progress))]
        {:style {:height (str percent "%")}})]]))