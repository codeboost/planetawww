(ns plawww.media-item.core
  (:require [reagent.core :as r]))

(defn item-info-component [state]
  (fn [_]
    (let [item (:selected-item @state)]
      (when item
        [:div.media-item-info-container
         [:div.media-item-info
          [:div.title (:title item)]]]))))

