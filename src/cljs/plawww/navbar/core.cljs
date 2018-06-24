(ns plawww.navbar.core
  (:require
   [plawww.navbar.search-component :refer [search-component]]
   [reagent.core :as r]))


(def state (r/atom {:search-string ""}))

(defn navbar []
  [:div.navbar
   [search-component state]])

