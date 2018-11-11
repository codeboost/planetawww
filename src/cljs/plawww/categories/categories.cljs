(ns plawww.categories.categories
  (:require [reagent.session :as session]
            [plawww.paths :as paths]
            [reagent.core :as r]))


(defn render-cat [i {:keys [id name slug]}]
  ^{:key id}
  [:li.category.show-scaled {:style {:animation-delay (str (* i 100) "ms")}}
   [:a {:href :#}
    [:img {:src (paths/category-image slug)}]
    [:div.title name]]])

(defn page []
  (let [cats (session/get :categories)]
    [:div.categories
     (into
      [:ul.cats]
      (map-indexed render-cat cats))]))


