;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.medialist.toolbar
  (:require
   [reagent.core :as r]))

(defn- toggle-item [text on? on-click]
  [:a.toggle-button
   {:on-click on-click
    :class (when on? :on)}
   text])

(defn- toggle-group [state toggle-k buttons]
  (fn []
    (into
     [:div.toggle-group]
     (for [[title k] buttons]
       [toggle-item
        title
        (= k (get-in @state toggle-k))
        #(swap! state assoc-in toggle-k k)]))))

(defn explorer-buttons [*state]
  (fn []
    (let [searching? (pos? (count (:search-string @*state)))]
      (when-not searching?
        [:div.toolbar.filters
         [toggle-group *state [:sort-by]
          [["A-Z" :title]
           ["VECHI" :old]
           ["NOI" :new]]]]))))
