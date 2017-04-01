;   Copyright (c) Braghis Florin. All rights reserved.
;   Released under GPL-3.0 license
;   https://opensource.org/licenses/GPL-3.0.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns plawww.search-component
  (:require [clojure.string :as str]
            [reagent.core :as r]))

(defn rotopt
  "Rotate option.
  Find `cur` and return (++index % length)."
  [options cur]
  (let [i (.indexOf options cur)
        i (mod (inc i) (count options))]
    (nth options i)))

(defn rotate-aopt [aopts opt options]
  "Swaps `opt` to next value in options."
  (let [cur (@aopts opt)
        nextv (rotopt options cur)]
    (swap! aopts conj (hash-map opt nextv))))


(defn- search-input[settings-atom]
  [:input.search-box {:type      "text"
                      :on-change #(swap! settings-atom conj {:search-string (-> % .-target .-value)})}])

(defn- toggle-button [text onclick]
  [:button.toggle-button {:on-click onclick} text])


(def view-modes [:plain :tag])

;Keep them separate. This will allow us to implement translation.
(def view-mode-titles {:tag   "TĂG"
                       :plain "BUC."})

(defn view-mode-button [aopts keyname]
  (let [title (view-mode-titles (@aopts keyname))]
    [toggle-button title #(rotate-aopt aopts keyname view-modes)]))

(defn- search-component-filters [search-settings]
  (let [{:keys [group-by display dirty]} @search-settings]
    [:div.filters
     [view-mode-button search-settings :group-by]
     (toggle-button display (fn []))]))

(defn search-component [search-prompt search-settings]
  [:div.search-component
   [:div.search-text search-prompt]
   [search-input search-settings]
   [search-component-filters search-settings]
   ])

