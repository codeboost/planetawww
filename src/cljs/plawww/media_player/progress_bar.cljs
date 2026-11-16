;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.media-player.progress-bar)

(defn clamp-v
  "Rounds off `v` to 0 if `v < vmin` and to 1 if `v > vmax`.
  v is a double value in the range 0..1"
  [v vmin vmax]
  (cond
    (< v vmin) 0
    (> v vmax) 1
    :else v))

(defn progress-bar [progress callback]
  ;currentTarget, not target: a click can land on the inner fill div,
  ;but the fraction must be computed against the whole bar.
  [:div.progress-bar {:on-click (fn [e]
                                  (let [el (.-currentTarget e)
                                        rect (.getBoundingClientRect el)
                                        width (.-width rect)
                                        offsetx (- (.-clientX e) (.-left rect))
                                        offsetx (if (<= offsetx 8) 0 offsetx) ;trim to 0 if clicked within 8 pixels.
                                        percent (if (pos? width) (/ offsetx width) 0)]
                                    (callback percent)))}
   [:div.progress-bar-progress
    (let [percent (* 100 (min 1 progress))]
      {:style {:width (str percent "%")
               :padding-left (if (and (> percent 0) (< percent 10)) "8px" "0px")}})]])

(defn vertical-progress-bar [progress callback]
  [:div.vertical-progress-bar
   {:on-click
    (fn [e]
      (let [el (.-currentTarget e)
            rect (.getBoundingClientRect el)
            height (.-height rect)
            click-pos (- (.-bottom rect) (.-clientY e))
            percent (if (pos? height) (/ click-pos height) 0)
            percent (clamp-v percent 0.1 0.95)]
        (callback percent)))}
   [:div.vertical-progress-bar-progress
    (let [percent (* 100 (min 1 progress))]
      {:style {:height (str percent "%")}})]])
