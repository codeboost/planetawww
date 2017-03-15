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


(defn mindic[]
  [:div.horiz-container.mindic
   [:img {:src "images/mindic-left.png"}]
   [:div.extend]
   [:img {:src "images/mindic-right.png"}]])

(defn nav-menu[]
  [:div.vert-container
   (mindic)
   [:div.horiz-container.nav-menu
    [:div.left-border]
    [:div.contents "Hello this is it"]
    [:div.right-border]
    ]])

(defn site-content[]
  [:div.horiz-container.site-content
   (nav-menu)
   [:div.vert-container.extender
    (mindic)
    [:div.contents "Hello this is the content"]]])

(defn all-site []
  [:div.vert-container.planeta-site
   (site-header/planeta-header)
   (site-content)])



(defcard-rg all-site (all-site))


(reagent/render [:div] (.getElementById js/document "app"))

;; remember to run 'lein figwheel devcards' and then browse to
;; http://localhost:3449/cards
