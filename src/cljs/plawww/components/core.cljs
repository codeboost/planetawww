(ns plawww.components.core
  (:require
   [reagent.core :as r]
   [cljsjs.typedjs]))

(defn typed-text-component
  "Renders a p element and applies typedjs on it, passing `opts` to it.
  See typed.js documentation for the potential options."
  [opts]
  (let [dom-el (atom nil)]
    (r/create-class
     {:component-did-mount
      (fn []
        (->
         (js/$ @dom-el)
         (.typed (clj->js opts))))
      :reagent-render
      (fn [] [:p.description {:ref #(reset! dom-el %)}])})))

(defn- toggle-accessory-button
  [state text key]
  [:div.accessory-button
   {:on-click #(swap! state update-in [key] not)
    :class    (when (@state key) :selected)}
   text])

(defn- minimise-button
  [state text key]
  [:div.min-button
   {:on-click #(swap! state update key not)
    :style {:cursor :pointer}}
   text])
