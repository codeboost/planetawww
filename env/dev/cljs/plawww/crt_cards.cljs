(ns plawww.crt-cards
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [plawww.core :as core]
            [devcards.core :as dc]
            [reagent.core :as reagent]
            [plawww.site-header :as site-header])
  (:require-macros
    [devcards.core
     :as dc
     :refer [defcard defcard-doc defcard-rg deftest]]))


(defn crt-interface-fn []
  [:div.frame
   [:div.piece.output
    [:h1 "Planeta Moldova"]]])

(defcard-rg crt-interface (crt-interface-fn))
