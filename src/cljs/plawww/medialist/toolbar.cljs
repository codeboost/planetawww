;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.medialist.toolbar
  (:require
   [reagent.core :as r]))

(defn- toggle-item [text on? on-click]
  [:div.toggle-button
   {:on-click on-click
    :class (when on? :on)}
   text])

(defn- toggle-group [buttons]
  (into
   [:div.toggle-group]
   (for [[title on? on-click] buttons]
     [toggle-item title on? on-click])))

(defn explorer-buttons [{:keys [sort-by clicked detail? detail-clicked]}]
  [:div.toolbar.filters
   [toggle-group
    [["A-Z"   (= sort-by :title) #(clicked :title)]
     ["VECHI" (= sort-by :old)   #(clicked :old)]
     ["NOI"   (= sort-by :new)   #(clicked :new)]
     ["DETALIAT?" detail? #(detail-clicked)]]]])
