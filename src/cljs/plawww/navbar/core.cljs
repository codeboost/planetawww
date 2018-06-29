(ns plawww.navbar.core
  (:require
   [plawww.navbar.search-component :refer [search-component]]
   [reagent.core :as r]))


(def state (r/atom {:search-string ""}))

(defn navbar []
  [:div.navbar
   [:div.home-button [:a.accessory-button {:href "/home"} "(*)"]]
   [search-component state]
   [:div]])

