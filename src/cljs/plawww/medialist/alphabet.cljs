;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.medialist.alphabet
  (:require
   [reagent.core :as reagent]
   [plawww.utils :as utils]))

(defn alphabet-items
  [letters selected on-click]
  [:ul
   {:style {:width (str (* 40 (count letters)) "px")}} ;40 the width of one letter item (see `byletters.less`).
   (for [letter letters]
     ^{:key letter}
     [:li [:a {:href "#"
               :on-click (fn [event] (when on-click
                                       (on-click letter event)))
               :class (if (utils/starts-with-letter? letter selected) "selected" "")} letter]])])

(defn- scroll-to-letter
  [letter]
  (let [element (document.querySelector ".alphabet .selected")]
    (when element
      (.scrollIntoView element {:block :start
                                :inline :center}))))

(defn- scroll-to-current-letter [*letter]
  (js/setTimeout
   (fn []
     (scroll-to-letter @*letter)
     20)))

