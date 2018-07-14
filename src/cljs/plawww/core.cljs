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
   [plawww.crt :refer [crt-page]]
   [plawww.home :refer [home-page]]
   [plawww.texts.core :as texts-section]
   [plawww.welcome :as welcome]
   [plawww.about.core :as about]
   [plawww.medialist.core :as media-page]
   [plawww.media-player.controller :as media-controller]
   [reagent.core :as reagent :refer [atom]]
   [reagent.session :as session]
   [secretary.core :as secretary :refer [defroute]]
   [plawww.media-player.core :as player]))


(defonce ^:export ALLMEDIA (js->clj js/kolbasulPlanetar :keywordize-keys true))

;; -------------------------
;; Views

(defn hook-up-the-stuff
  []
  (session/put! :media-items ALLMEDIA)
  (media-controller/hook-up-the-stuff))

(defn- media-browser-page []
  [crt-page
   [media-page/media-page ALLMEDIA]])

(defn- show-media-browser [& [opts :or {}]]
  (let [;Force nil for :included-tags so that it is applied during the `merge`.
        opts (update opts :included-tags identity)]
    (js/console.log "show-media-browser: " opts)
    (session/put! :current-page #'media-browser-page)
    (media-page/set-opts opts)))

(defn about-page []
  [crt-page
   [about/page]])

;Home - shown when (*) is clicked
(defn show-home-page []
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

(defroute #"/home/?" []
  (session/put! :current-page #'show-home-page))


(defroute #"/text/?|/carti/?" []
  (show-text-page {:sub-menu nil}))

(defroute "/text/:page" [page]
  (show-text-page {:sub-menu page}))

(defroute "/carti/:page" [page]
  (show-text-page {:sub-menu (keyword page)}))


(defroute #"/media/?" [q]
  (show-media-browser))

(defroute #"/media/(\d+)" [id q]
  (show-media-browser)
  (if-let [item (media-item-for-id (js/parseInt id))]
          (media-controller/set-current-media-item item)
    (js/console.log "Could not find media item for id " id)))

(defroute #"/media/letter/?([a-zA-Z])?" [letter]
  (show-media-browser {:cur-letter (or letter "A") :group-by :plain}))

(defroute #"/media/tag/?" []
  (show-media-browser {:included-tags #{} :group-by :tag}))

(defroute "/media/tag/:tag" [tag]
  (let [tag-set (set (str/split tag #"\+"))]
    (show-media-browser {:included-tags tag-set :group-by :tag})))


;(secretary/locate-route "/media/tag/ab")

;; -------------------------
;; Initialize app

(defn current-page []
  (let [page (session/get :current-page)]
    (if page
      [:div [page]
       [:div [player/player]]]
      [:div "Dapu-kaneshna-kiar-amush ! Nu-i asa ceva, nu-i ! "])))

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-up-the-stuff)
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))
