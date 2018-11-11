(ns plawww.categories.categories
  (:require [reagent.session :as session]
            [plawww.paths :as paths]
            [cljsjs.react-transition-group]
            [reagent.core :as r]))

(def css-transition-group
  (r/adapt-react-class js/ReactTransitionGroup.TransitionGroup))

(def css-transition
  (r/adapt-react-class js/ReactTransitionGroup.Transition))


(defn render-cat [{:keys [id name slug]}]
  ^{:key id}
   [:li.category
    [:a {:href :#}
     [:img {:src (paths/category-image slug)}]
     [:div.title name]]])

(defn categories-comp []
  (let [cats (session/get :categories)]
    [:div.categories
     (into
      [:ul.cats]
      (mapv render-cat cats))]))

(defn test-cats []
  (let [cats (r/atom [])]
    (fn []
      [:div.categories
       [:a.toggle-button
        {:href     :#
         :on-click #(swap! cats conj {:id   (random-uuid)
                                      :name (random-uuid)
                                      :slug "origini"})}
        "Add"]
       [:a.toggle-button
        {:href     :#
         :on-click #(reset! cats [])}
        "Clear"]
       (into
        [:ul.cats]
        (mapv render-cat @cats))])))
(defn page []
  (categories-comp))

