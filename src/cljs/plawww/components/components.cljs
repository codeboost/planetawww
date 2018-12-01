;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.components.components
  (:require
   [reagent.core :as r]
   [cljsjs.typedjs]
   [plawww.paths :as paths]))

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

(defn- minimise-button
  [text on-click]
  [:div.min-button
   {:on-click on-click
    :style {:cursor :pointer}}
   text])


(defn tag-list-component [tags on-click]
  (into
   [:ul.tags]
   (for [tag tags]
     (let [tag-text tag]
       [:li
        [:a
         {:href     (paths/explorer-path (str "tag/" tag))
          :on-click on-click}
         tag-text]
        " "]))))