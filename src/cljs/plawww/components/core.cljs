(ns plawww.components.core
  (:require
   [reagent.core :as r]
   [cljsjs.typedjs]))

(defn typed-text-component
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