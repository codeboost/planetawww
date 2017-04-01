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


(defn- search-input [settings-atom]
  [:input.search-box {:type      "text"
                      :on-change #(swap! settings-atom conj {:search-string (-> % .-target .-value)})}])

(defn- toggle-button [text cls onclick]
  [:button.toggle-button {:on-click onclick
                          :class cls} text])

(defn option-button
  "Creates a toggle button which rotates through a list"
  [aopts keyname options titles & [classes]]
  (let [cur (@aopts keyname)
        title (or (titles cur)
                  (name keyname))
        cls (if classes (classes cur) "")]
    (toggle-button title cls #(rotate-aopt aopts keyname options))))

;----------------------

(def group-modes [:plain :tag])

;Keep them separate. This will allow us to implement translation.
(def group-mode-titles {:tag   "BUC"
                        :plain "TĂG"})


(defn group-mode-button [aopts]
  (option-button aopts :group-by group-modes group-mode-titles))

;----------------------

(def item-view-modes [:plain :detail])
(def item-view-mode-titles {:plain  "DET"
                            :detail "TXT"})

(defn item-view-button [aopts]
  (option-button aopts :item-view-mode item-view-modes item-view-mode-titles))

;----------------------

(defn toggle-expand-all-button [aopts]
  (option-button aopts :expand-all? [true false]
                 {true " + " false " - "}
                 {true "on" false ""}))

;----------------------

(defn- search-component-filters [search-settings]
  (let [{:keys [group-by display dirty]} @search-settings]
    [:div.filters
     (group-mode-button search-settings)
     (toggle-expand-all-button search-settings)
     (item-view-button search-settings)]))

(defn search-component [search-prompt search-settings]
  [:div.search-component
   [:div.search-text search-prompt]
   [search-input search-settings]
   [search-component-filters search-settings]
   ])

