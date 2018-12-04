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

(defn search-results [ss {:keys [on-close]}]
  [medialist/render-search-results
   (session/get :media-items)
   ss
   [search-component/random-not-found-msg (session/get :xx?)]
   on-close])

(defn crt-page [_ & _]
  (fn [page & [{:keys [detail-page navbar-hidden?] :as opts}]]
    (let [ss (:search-string @navbar/state)
          searching? (not (empty? ss))]
      [:div.vert-container
       [:div.tv.noisy
        [:div.frame.tv
         [:div.piece.output
          [:div.planeta-experience
           [:div.page-layout
            [:div.primary {:class (when detail-page :detailed)}
             (when-not navbar-hidden?
               [:div.nav-area [navbar]])
             (when (and searching? (not navbar-hidden?))
               [search-results ss {:on-close #(swap! navbar/state assoc :search-string "")}])

             [:div.app-page page]]
            (when detail-page
              [:div.detail {:class (when detail-page :detailed)} detail-page])]
           [:div.player-space]]]]]])))


    
