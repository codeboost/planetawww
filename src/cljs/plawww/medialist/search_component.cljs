;   Copyright (c) Braghis Florin. All rights reserved.
;   Released under GPL-3.0 license
;   https://opensource.org/licenses/GPL-3.0.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.medialist.search-component
  (:require [clojure.string :as str]
            [reagent.core :as r]))

;Can be done better, but I'll leave it as testament of how learning improves over time.
;This means that hopefully I now write better clojure than when I wrote this.

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
                      :on-change #(swap! settings-atom conj {keyname (-> % .-target .-value)})
                      :value (get @settings-atom keyname)}])

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
    [toggle-button title cls #(rotate-aopt aopts keyname options)]))

(defn group-mode-button
  "Sugar for group-by button"
  [*opts]
  [option-button *opts :group-by {:tag   "LIT"
                                  :plain "TAG"}])



(defn- button-all [*state]
  (fn []
    [:button.toggle-button {:on-click #(swap! *state update-in [:show-all?] not)
                            :class-name (when (:show-all? @*state) :on)} "TOT"]))


(defn- button-tags [selected?]
  [:button.toggle-button {:class-name (if selected? :on "")}
   [:a {:href "/media/tag"} "TAGURI"]])

(defn- button-letters [selected?]
  [:button.toggle-button {:class-name (if selected? :on "")}
   [:a {:href "/media/letter"} "BUCHII"]])

(defn- search-component-filters [*state]
  (print "State is: " @*state)
  [:div.filters
   [button-all *state]
   [button-tags (= :tag (:group-by @*state))]
   [button-letters (= :plain (:group-by @*state))]])


(defn random-search-prompt []
  (let [prompts ["CE DORITI?"
                 "CU CE VA PUTEM SERVI?"
                 "SRCH:"
                 "CAUT:"
                 "SI VREI?"
                 "PRAPADIT?"
                 "TOARNA:"
                 "SHOPTESHTE:"
                 "AMNEZIE?"
                 "VEI GASI:"
                 "PROBLEME?"]
        index (rand-int (count prompts))]
    (nth prompts index)))

(defn search-component [*state]
  [:div.search-component
   [:div.search-text (random-search-prompt)]
   [search-input *state :search-string]
   [search-component-filters *state]])





(comment
 (defn item-view-button
   "Sugar for item-view-mode button"
   [aopts]
   (option-button aopts :item-view-mode {:plain  "DET"
                                         :detail "TXT"}))

 (defn on-off-button [aopts keyname txts]
   (option-button aopts :expand-all?
                  (zipmap [true false] txts)
                  {true "on" false ""}))

 (defn toggle-expand-all-button [aopts]
   (on-off-button aopts :expand-all? [" + " " - "])))
