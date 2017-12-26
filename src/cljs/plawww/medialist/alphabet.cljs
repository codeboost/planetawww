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
     [:li [:a {:href (str "/media/letter/" letter)
               :class (if (utils/starts-with-letter? letter selected) "selected" "")} letter]])])

(defn- scroll-to-letter
  [letter]
  (let [element (document.querySelector ".alphabet .selected")]
    (when element
      (.scrollIntoView element {:block :start
                                :inline :start}))))

(defn- scroll-to-current-letter [*letter]
  (js/setTimeout
   (fn []
     (scroll-to-letter @*letter)
     20)))

(defn alphabet-component
  "Displays a list of letters in the `letters` vector and when the letter is clicked,
  it is stored in the `*letter` atom."
  [*letter letters]
  (fn []
    (scroll-to-current-letter *letter)
    [:div.alphabet
     [alphabet-items
      letters
      @*letter]]))
