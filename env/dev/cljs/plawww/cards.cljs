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

(defn crt-interface-fn []
  [:iframe {:src "/"
            :width "100%"
            :height "640px"}])

(defcard-rg crt-interface (crt-interface-fn))


(reagent/render [:div] (.getElementById js/document "app"))

;; remember to run 'lein figwheel devcards' and then browse to
;; http://localhost:3449/cards
