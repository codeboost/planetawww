(ns plawww.search-component
  (:require [clojure.string :as str]
            [reagent.core :as r]))

(defn- search-input[settings-atom]
  [:input.search-box {:type      "text"
                      :on-change #(swap! settings-atom conj {:search-string (-> % .-target .-value)})}])

(defn- toggle-button [text onclick]
  [:button.toggle-button {:on-click onclick} text])

(defn- next-group-by[cur]
  ({:tag :plain
    :plain :tag} cur))

(defn- search-component-filters [search-settings]
  (let [{:keys [group-by display dirty]} @search-settings]
    [:div.filters
     (toggle-button group-by (fn []
                               (swap! search-settings conj {:group-by (next-group-by group-by)})))
     (toggle-button display (fn []
                              ))]))

(defn search-component [search-prompt search-settings]
  [:div.search-component
   [:div.search-text search-prompt]
   [search-input search-settings]
   [search-component-filters search-settings]
   ])

