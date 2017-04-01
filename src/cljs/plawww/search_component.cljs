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


(defn- search-input
  "Renders the search input box and swaps the :search-string "
  [settings-atom keyname]
  [:input.search-box {:type      "text"
                      :on-change #(swap! settings-atom conj {keyname (-> % .-target .-value)})}])

(defn- toggle-button [text cls onclick]
  [:button.toggle-button {:on-click onclick
                          :class cls} text])

(defn option-button
  "Creates a toggle button which rotates through a list of values.
  On click, the next value is swapped into the `keyname` value of the aopts atom.
  opts is a map, where each key is a state and it's value is the button's text for that state."
  [aopts keyname opts & [classes]]
  (let [cur (@aopts keyname)
        title (or (opts cur)
                  (name keyname))
        cls (if classes (classes cur) "")
        options (keys opts)]
    (toggle-button title cls #(rotate-aopt aopts keyname options))))

;----------------------

(defn group-mode-button
  "Sugar for group-by button"
  [aopts]
  (option-button aopts :group-by {:tag   "BUC"
                                  :plain "TĂG"}))

;----------------------

(defn item-view-button
  "Sugar for item-view-mode button"
  [aopts]
  (option-button aopts :item-view-mode {:plain  "DET"
                                        :detail "TXT"}))

;----------------------

(defn on-off-button [aopts keyname txts]
  (option-button aopts :expand-all?
                 (zipmap [true false] txts)
                 {true "on" false ""}))

(defn toggle-expand-all-button [aopts]
  (on-off-button aopts :expand-all? [" + " " - "]))

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
   [search-input search-settings :search-string]
   [search-component-filters search-settings]
   ])

