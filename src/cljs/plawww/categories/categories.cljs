(ns plawww.categories.categories
  (:require [reagent.session :as session]
            [plawww.paths :as paths]
            [reagent.core :as r]))

(defn category-component [i {:keys [name slug]} & [url]]
  (let [url (or url (paths/explorer-path (str "?colectia=" slug)))]
    [:div.category.scale-on-hover
     [:div.cat-container.show-scaled {:style {:animation-delay  50 * i}}
      [:a {:href url}
       [:img.category-image {:src (paths/category-image slug)}]
       [:div.title name]]]]))

(defn render-cat [i {:keys [id] :as category}]
  ^{:key id} [:li [category-component i category]])

(defn page []
  (let [cats (session/get :categories)]
    [:div.categories-outer
     [:h1.logo "PLANETA MOLDOVA"]
     [:div.categories
      (into
       [:ul.cats]
       (map-indexed render-cat cats))]]))


