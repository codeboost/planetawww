;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.crt
  (:require [plawww.media-player.core :as player]
            [plawww.navbar.core :refer [navbar]]
            [plawww.medialist.search-results :as medialist]
            [reagent.session :as session]
            [plawww.navbar.core :as navbar]
            [plawww.navbar.search-component :as search-component]
            [clojure.string :as str]))

(defn search-results [state]
  (let [ss (:search-string @state)]
    [medialist/render-search-results
     (session/get :media-items)
     ss
     [search-component/random-not-found-msg (session/get :xx?)]
     #(swap! state assoc :search-string "")]))

(defn page-or-search-results [page *state]
  (fn []
    (let [ss (:search-string @*state)
          searching? (not (empty? ss))]
      (when searching?
        [search-results *state ss]))))

(defn crt-page [page & [{:keys [navbar?]
                         :or {navbar? true}}]]
  [:div.vert-container
   [:div.tv.noisy
    [:div.frame.tv
     [:div.piece.output
      [:div.planeta-experience
       (when navbar? [navbar])
       [page-or-search-results page navbar/state]
       page]]]]])
    
