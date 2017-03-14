(ns plawww.site-header
  (:require [reagent.core :as reagent :refer [atom]]))


(defn image [name]
  [:img {:src (str "images/" name)}])

(def nbsp {:dangerouslySetInnerHTML {:__html "&nbsp;"}})

(defn logo-lhead []
  [:div.horiz-container.lhead
   (image "lhead-left.png")
   [:div.extender]
   (image "lhead-right.png")])

(defn logo-mhead []
  [:div.horiz-container.mhead
   (image "mhead-left.png")
   [:div.extender]
   (image "mhead-right.png")
   [:div.logo (image "planeta-logo-text.png")]])

(defn planeta-header []
  [:div.horiz-container.planeta-header
   (logo-lhead)
   (logo-mhead)])




(comment
  (reagent/render-to-static-markup (logo-right-side))
  (reagent/render-to-static-markup [:table {:width 200
                                            :cell-spacing 0
                                            :cell-padding 0} [:tr [:td "Hello"]]]))
