;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.core
  (:require
   [accountant.core :as accountant]
   [cljs.core.async :refer [put!]]
   [cljsjs.typedjs]
   [clojure.string :as str]
   [plawww.barul.core :as barul]
   [plawww.crt :refer [crt-page]]
   [plawww.home :refer [home-page]]
   [plawww.paths :refer [explorer-path]]
   [plawww.texts.core :as texts-section]
   [plawww.welcome :as welcome]
   [plawww.about.core :as about]
   [plawww.medialist.explorer :as explorer]
   [reagent.core :as reagent :refer [atom]]
   [reagent.session :as session]
   [secretary.core :as secretary :refer [defroute]]
   [plawww.media-player.core :as player]))

(defonce ^:export ALLMEDIA (js->clj js/kolbasulPlanetar :keywordize-keys true))

;; -------------------------
;; Views

(defn about-page []
  [crt-page
   [about/page]])

(defn barul-page []
  [crt-page
   [barul/page]])

(defn explorer-page []
  (fn []
    [crt-page
     [explorer/explorer-page]]))

;Home - shown when (*) is clicked
(defn show-home-page []
  (swap! plawww.navbar.core/state assoc :search-string "")
  [crt-page
   [home-page]])



;Texte si Carti
(defn render-text-page [] [crt-page [texts-section/main-menu]])

(defn show-text-page [& [opts]]
  (swap! texts-section/state merge opts)
  (session/put! :current-page #'render-text-page))

(defn media-item-for-id [id]
  (first (filter #(= id (:id %)) ALLMEDIA)))

(defn show-explorer-page [id]
  (when-not (= (session/get :current-page) #'explorer-page)
    (session/put! :current-page #'explorer-page))
  (let [item (and id (media-item-for-id (js/parseInt id)))]
    (player/set-detail-visible false)
    (session/put! :current-media-item item)))

;; -------------------------
;; Routes

(defn explorer-path-regex [subpath]
  (re-pattern (explorer-path subpath)))

(secretary/set-config! :prefix "#")

(defroute "/" []
  (welcome/on-init)
  (session/put! :current-page #'welcome/page))

(defroute "/about" []
  (session/put! :current-page #'about-page))

(defroute #"/barul/?" []
  (session/put! :current-page #'barul-page))

(defroute (explorer-path-regex "?") []
  (show-explorer-page nil))

(defroute (explorer-path-regex "(\\d+)") [id q]
  (show-explorer-page id))

(defroute (explorer-path-regex "tag/?") []
  (show-explorer-page nil)
  (explorer/set-opts {:tag-editor-visible? true}))

(defroute (explorer-path "tag/:tag") [tag]
  (let [tag-set (set (str/split tag #"\+"))]
    (show-explorer-page nil)
    (explorer/set-opts {:included-tags tag-set})))


(defroute #"/home/?" []
  (session/put! :current-page #'show-home-page))


(defroute #"/text/?|/carti/?" []
  (show-text-page {:sub-menu nil}))

(defroute "/text/:page" [page]
  (show-text-page {:sub-menu page}))

(defroute "/carti/:page" [page]
  (show-text-page {:sub-menu (keyword page)}))

;; -------------------------
;; Initialize app

(defn current-page []
  (let [page-cursor (session/cursor [:current-page])
        current-item-cursor (session/cursor [:current-media-item])]
    (fn []
      (let [page @page-cursor
            current-item @current-item-cursor]
        (if page
          [:div [page]
           ;The things below are not affected by page scrolling
           [:div [player/player]]]
          [:div "Dapu-kaneshna-kiar-amush ! Nu-i asa ceva, nu-i ! "])))))

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (session/put! :media-items (explorer/parse-dates ALLMEDIA))
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))
