(ns plawww.classic
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


(defn menu-item [title href]
  [:li.menu-item [:a {:href href} title]])

(def menu-items ["KIDOASE", "PRAGOANE", "PEREDOAZE", "GRUZURI", "VIDEO"])


(defn nav-menu[]
  [:div.vert-container.nav-menu
   [:div.horiz-container
    [:ul.contents.extender
     (map #(menu-item % "") menu-items)]
    ]])

(defn site-content[]
  [:div.horiz-container.site-content
   (nav-menu)
   [:div.vert-container.extender
    [:div.contents "Hello this is the content !!!"]]])

(defn all-site []
  [:div.vert-container.planeta-site
   (site-header/planeta-header)
   (site-content)])

(defn all-site2 []
  [:div.horiz-container.planeta
   [:div.vert-container.master-container
    (site-header/lhead)
    [:div.horiz-container
     [:div.left-border]
     (nav-menu)
     [:div.right-border]]]
   [:div.vert-container.detail
    (site-header/mhead)
    [:div.content]
    ]
   ])
(defcard-rg all-site (all-site2))