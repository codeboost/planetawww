(ns plawww.categories.categories
  (:require [reagent.session :as session]
            [plawww.paths :as paths]
            [reagent.core :as r]))


(defn render-cat [i {:keys [id name slug]} & [url]]
  (let [url (or url (paths/explorer-path (str "?colectia=" slug)))]
    ^{:key id}
    [:li.category.scale-on-hover
     [:div.cat-container.show-scaled {:style {:animation-delay (str (* i 100) "ms")}}
      [:a {:href url}
       [:img {:src (paths/category-image slug)}]
       [:div.title name]]]]))

(defn page []
  (let [cats (session/get :categories)]
    [:div.categories-outer
     [:h1.logo "PLANETA MOLDOVA"]
     [:div.categories
      (into
       [:ul.cats]
       (map-indexed render-cat cats))]]))


