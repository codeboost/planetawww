;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.navbar.core
  (:require
   [plawww.navbar.search-component :refer [search-component]]
   [reagent.core :as r]))

(def state (r/atom {:search-string ""}))

(defn navbar []
  [:div.navbar
   [:div.home-button [:a.accessory-button {:href "/home"} "(*)"]]
   [search-component state]
   [:div]])

