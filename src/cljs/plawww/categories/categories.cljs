(ns plawww.categories.categories
  (:require [reagent.session :as session]
            [plawww.paths :as paths]
            [reagent.core :as r]))

(defn category-component
  "Returns a category/collection component hiccup, class name .category.
  First argument is a map, the category, and the second is also a map, with the following keys:
    url - if present, will be used as href for when category clicked. If absent, will use default path
    scale-on-hover? - apply scale animation when mouse over.
    index - Index in the collection, used to control the animation delay."
  [{:keys [name slug]} {:keys [url scale-on-hover? index]
                        :or {scale-on-hover? true}}]
  (let [url (or url (paths/explorer-path (str "?colectia=" slug)))]
    [:div.category {:class (when scale-on-hover? :scale-on-hover)}
     [:div.cat-container.show-scaled {:style {:animation-delay  50 * index}}
      [:a {:href url}
       [:img.category-image {:src (paths/category-image slug)}]
       [:div.title name]]]]))

(defn- render-cat [i {:keys [id] :as category}]
  ^{:key id} [:li [category-component category {:index i}]])

(defn page []
  (let [cats (session/get :categories)]
    [:div.categories-outer
     [:h1.logo "PLANETA MOLDOVA"]
     [:div.categories
      (into
       [:ul.cats]
       (map-indexed render-cat cats))]]))


