;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.navbar.core
  (:require
   [plawww.navbar.search-component :refer [search-component]]
   [reagent.core :as r]
   [plawww.media-player.core :as media-player]
   [reagent.session :as session]))

(defonce state (r/atom {:search-string ""}))

(defn navbar []
  (let [minimised-cursor (session/cursor [:navbar-minimised?])]
    (fn []
      (let [minimised? @minimised-cursor]
        [:div.navbar
         [:div.home-button [:a.accessory-button {:href "/home"} "(*)"]]
         (when-not minimised?
           [search-component {:search-string (:search-string @state)
                              :on-change (fn [e]
                                           ;I don't like this, but will do for now; this thing needs a proper refactor.
                                           (media-player/set-detail-visible false)
                                           (swap! state assoc :search-string (-> e .-target .-value)))
                              :search-title-clicked #(session/put! :navbar-minimised? true)}])

         (when minimised?
           [:h4.max-button {:on-click #(session/put! :navbar-minimised? false)} "?"])

         [:div]]))))

