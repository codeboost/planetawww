;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.medialist.toolbar
  (:require
   [reagent.core :as r]))

(defn- toggle-item [text tooltip on? on-click]
  [:div.toggle-button
   {:on-click on-click
    :class (when on? :on)
    :title tooltip}
   text])

(defn- toggle-group [buttons]
  (into
   [:div.toggle-group]
   (for [[text tooltip on? on-click ] buttons]
     [toggle-item text tooltip on? on-click])))

(defn alphabetic-button [sort-by clicked]
  (if (= sort-by :title)
    ["Z-A" "Invers" (#{:title :title-desc} sort-by) #(clicked :title-desc)]
    ["A-Z" "Alfabetic" (#{:title :title-desc} sort-by) #(clicked :title)]))

(defn randomize-button [sort-by clicked]
  (if (= sort-by :random)
    ["R!" "Randomizeaza !" (#{:random :random-1} sort-by) #(clicked :random-1)]
    ["R!" "Randomizeaza !" (#{:random :random-1} sort-by) #(clicked :random)]))

(defn explorer-buttons [{:keys [sort-by clicked detail? detail-clicked]}]
   [toggle-group
    [(alphabetic-button sort-by clicked)
     ["VECHI" "Mai clasice asa"(= sort-by :old)   #(clicked :old)]
     ["NOI"   "Prospaturi" (= sort-by :new)   #(clicked :new)]
     (randomize-button sort-by clicked)
     ["D?" "Detalii suplimentare" detail? #(detail-clicked)]]])
