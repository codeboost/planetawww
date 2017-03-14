(ns plawww.cards
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


(defcard-rg site-logo (site-header/planeta-header))

;(defcard-rg home-page-card
;  [core/home-page])

(reagent/render [:div] (.getElementById js/document "app"))

;; remember to run 'lein figwheel devcards' and then browse to
;; http://localhost:3449/cards
