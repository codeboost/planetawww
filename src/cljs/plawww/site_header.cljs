(ns plawww.site-header
  (:require [reagent.core :as reagent :refer [atom]]))


(defn image [name]
  [:img {:src (str "images/" name)}])

(def nbsp {:dangerouslySetInnerHTML {:__html "&nbsp;"}})

(defn lhead []
  [:div.horiz-container.lhead
   (image "lhead-left.png")
   [:div.extender]
   (image "lhead-right.png")
   [:div.content-c
    [:div.content-i
     (image "pm-logo.png")]]])

(defn mhead []
  [:div.horiz-container.mhead
   (image "mhead-left.png")
   [:div.extender]
   (image "mhead-right.png")
   [:div.logo (image "planeta-logo-text.png")]])

(defn planeta-header []
  [:div.horiz-container.planeta-header
   (lhead)
   (mhead)])






(comment
  (reagent/render-to-static-markup (logo-right-side))
  (reagent/render-to-static-markup [:table {:width 200
                                            :cell-spacing 0
                                            :cell-padding 0} [:tr [:td "Hello"]]]))
