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
   [plawww.welcome :as welcome]
   [plawww.medialist.core :as media-page]
   [plawww.media-player.controller :as media-controller]
   [reagent.core :as reagent :refer [atom]]
   [reagent.session :as session]
   [secretary.core :as secretary :refer [defroute]]))


(defonce ALLMEDIA (:media (js->clj js/kolbasulPlanetar :keywordize-keys true)))

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
  (js/console.log "show-media-browser: " opts)
  (session/put! :current-page #'media-browser-page)
  (media-page/set-opts opts))

(defn about-page []
  [:div [:h2 "About plawww?"]
   [:div [:a {:href "/"} "go to the home page"]]])

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
  (show-media-browser {:expanded-tags #{""}}))

(defroute "/media/tag/:tag" [tag]
  (show-media-browser {:expanded-tags #{tag} :group-by :tag}))


;(secretary/locate-route "/media/tag/ab")

;; -------------------------
;; Initialize app

(defn current-page []
  (let [page (session/get :current-page)]
    (if page
      [:div [page]]
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
