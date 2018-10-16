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
   [plawww.texts.core :as texts-section]
   [plawww.welcome :as welcome]
   [plawww.about.core :as about]
   [plawww.media-item.media-item :as media-item]
   [plawww.medialist.core :as media-page]
   [plawww.medialist.explorer :as explorer]
   [reagent.core :as reagent :refer [atom]]
   [reagent.session :as session]
   [secretary.core :as secretary :refer [defroute]]
   [plawww.media-player.core :as player]))

(defonce ^:export ALLMEDIA (js->clj js/kolbasulPlanetar :keywordize-keys true))

;; -------------------------
;; Views

(defn- media-browser-page []
  [crt-page
   [explorer/explorer-page]])

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

;; -------------------------
;; Routes

(secretary/set-config! :prefix "#")

(defroute "/" []
  (welcome/on-init)
  (session/put! :current-page #'welcome/page))

(defroute "/about" []
  (session/put! :current-page #'about-page))

(defroute #"/barul/?" []
  (session/put! :current-page #'barul-page))


(defroute #"/explorer/?" []
  (session/put! :current-media-item nil)
  (session/put! :current-page #'explorer-page))

(defroute #"/explorer/(\d+)" [id q]
          (js/console.log (session/get :current-page))
  (when (not= 'explorer-page (session/get :current-page))
    (session/put! :current-page #'explorer-page))
  (let [item (media-item-for-id (js/parseInt id))]
    (session/put! :current-media-item item)))


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
  (let [page (session/get :current-page)
        current-item (session/get :current-media-item)]
    (if page
      [:div [page]
       ;The things below are not affected by page scrolling
       [:div [player/player]]
       (when current-item
         [:div [media-item/item-info-component
                {:on-play (fn []
                            (plawww.media-player.core/set-current-item current-item)
                            (session/put! :current-media-item nil))} {:selected-item current-item}]])]
      [:div "Dapu-kaneshna-kiar-amush ! Nu-i asa ceva, nu-i ! "])))

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
