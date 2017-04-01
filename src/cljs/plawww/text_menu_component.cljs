;   Copyright (c) Braghis Florin. All rights reserved.
;   Released under GPL v3 license
;   https://opensource.org/licenses/GPL-3.0.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.text-menu-component
  (:require [clojure.string :as str]
            [reagent.core :as r]))

(defn toggle-atom-on-click [a]
  (fn [e]
    (.preventDefault e)
    (reset! a (not @a))))

(defn class-for-title [expanded?]
  (if expanded? "opened" ""))

(defn menu->hiccup [{:keys [title items] :as menu} expanded?]
  "Renders a menu and its items"
  [:div.menu
   [:div.title [:a {:on-click (toggle-atom-on-click expanded?)
                    :class    (class-for-title @expanded?)} title]]
   (when (and @expanded? (pos? (count items)))
       (into [:ul.items] items))])
