(ns plawww.medialist.alphabet
  (:require [plawww.utils :as utils]))


(defn alphabet-items [letters selected on-click]
  [:ul
   (for [letter letters]
     ^{:key letter}
     [:li [:a {:href (str "/media/letter/" letter)
               :class (if (utils/starts-with-letter? letter selected) "selected" "")} letter]])])


(defn alphabet-component
  "Displays a list of letters in the `letters` vector and when the letter is clicked,
  it is stored in the `*letter` atom."
  [*letter letters]
  [:div.alphabet
   [alphabet-items
    letters
    @*letter
    #(reset! *letter %)]])
