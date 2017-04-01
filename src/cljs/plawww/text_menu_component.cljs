;   Copyright (c) Braghis Florin. All rights reserved.
;   Released under GPL v3 license
;   https://opensource.org/licenses/GPL-3.0.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.text-menu-component
  (:require [clojure.string :as str]
            [reagent.core :as r]))

(def *show-indexes* false)

(def menu-back-text "INAPOI")

(defn menu-text [index text]
  "Returns a formatted menu item with the index prepended.
  `index` is 0-based, but will be rendered as 1-based (eg. increased by 1)"
  (str (when *show-indexes* (str (inc index) ". ")) text))

(defn- back-menu-item [index]
  "Generic back menu item"
  ^{:key 999999} [:li [:a {:href "javascript:history.go(-1);"} (menu-text index menu-back-text)]])

(defn- menu-item-tag [index {:keys [text handler id]}]
  "Menu item to hiccup."
  (let [text (menu-text index text)
        href (if handler handler "/menu/")]
    ^{:key index} [:li [:a {:href href} text]]))

(defn toggle-atom-on-click [a]
  (fn [e]
    (.preventDefault e)
    (reset! a (not @a))))

(defn class-for-menu-title [expanded?]
  (if expanded? "opened" ""))




(defn menu->hiccup [{:keys [title items] :as menu} expanded?]
  "Renders a menu and its items"
  (let [show-back? false]
      [:div.menu
       [:div.title [:a {:on-click (toggle-atom-on-click expanded?)
                        :class (if @expanded? "opened" "")} title]]
       (when (and @expanded? (pos? (count items)))
         (let [list-items (vec (map-indexed menu-item-tag items))
               list-items (if show-back?
                            (conj list-items (back-menu-item (count items)))
                            list-items)]
           (into [:ul.items] list-items)))]))
