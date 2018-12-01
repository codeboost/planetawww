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
   [plawww.categories.categories :as categories]
   [plawww.crt :refer [crt-page]]
   [plawww.home :refer [home-page]]
   [plawww.paths :refer [explorer-path categories-path]]
   [plawww.texts.core :as texts-section]
   [plawww.welcome :as welcome]
   [plawww.about.core :as about]
   [plawww.medialist.explorer :as explorer]
   [reagent.core :as reagent :refer [atom]]
   [reagent.session :as session]
   [secretary.core :as secretary :refer [defroute]]
   [plawww.media-player.core :as player]
   [plawww.utils :as utils]
   [plawww.media-item.media-item :as media-item]))

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
  (let [current-item-cursor (session/cursor [:current-media-item])
        playing-item-cursor (player/state-cursor [:item])
        playing-state-cursor (player/state-cursor [:playing])]
    (fn []
      (let [current-item @current-item-cursor
            playing-item @playing-item-cursor
            playing? @playing-state-cursor]
        [crt-page
         [explorer/explorer-page]
         (when current-item
           [media-item/item-info-component
            {:on-play #(do
                         (player/set-current-item current-item))
             :on-close #(session/put! :current-media-item nil)
             :selected-item current-item
             :playing-item playing-item
             :playing? playing?}])]))))

(defn categories-page []
  (fn []
    [crt-page
     [categories/page]]))

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
  (first (filter #(= id (:id %)) (session/get :media-items))))

(defn category-for-slug [slug]
  (plawww.mediadb.core/category-by-slug (session/get :categories) slug))

(defn show-explorer-page [id & [opts]]
  (when-not (= (session/get :current-page) #'explorer-page)
    (session/put! :current-page #'explorer-page))
  (explorer/set-opts (or opts {:included-tags #{}}))
  (let [item (and id (media-item-for-id (js/parseInt id)))]
    (session/put! :current-media-item item)))

(defn show-categories-page []
  (session/put! :current-page #'categories-page))

(defn category-from-query-params [qp]
  (let [slug (get-in qp [:query-params :colectia])]
    (when slug
      (plawww.mediadb.core/category-by-slug (session/get :categories) slug))))

;; -------------------------
;; Routes

(defn explorer-path-regex [subpath]
  (re-pattern (explorer-path subpath)))

(defn categories-path-regex [subpath]
  (re-pattern (categories-path subpath)))

(secretary/set-config! :prefix "#")

(defroute "/" []
  (welcome/on-init)
  (session/put! :current-page #'welcome/page))

(defroute "/about" []
  (session/put! :current-page #'about-page))

(defroute #"/barul/?" []
  (session/put! :current-page #'barul-page))

(defroute (explorer-path-regex "?") [p qp]
  (js/console.log qp)
  (show-explorer-page nil {:category (category-from-query-params qp)
                           :included-tags #{}}))

(defroute (explorer-path-regex "(\\d+)") [id qp]
  (show-explorer-page id))

(defroute (explorer-path-regex "tag/?") []
  (show-explorer-page nil {:tag-editor-visible? true}))

(defroute (explorer-path "tag/:tag") [tag]
  (let [tag-set (set (str/split tag #"\+"))]
    (show-explorer-page nil {:included-tags tag-set
                             :category nil})))

(defroute (categories-path-regex "?") []
  (show-categories-page))

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
        player-item (player/state-cursor [:item])]
    (fn []
        (let [page @page-cursor]
          (if page
            [:div
             [page]
             (when @player-item
               [player/player])]
            [:div "Dapu-kaneshna-kiar-amush ! Nu-i asa ceva, nu-i ! "])))))

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn parse-dates [media-items]
  (map #(update-in % [:publish_on] (fn [s]
                                     (when s
                                       (js/Date. s)))) media-items))
(defn init! []
  (utils/ga "create" "UA-128602722-1" "auto")
  (session/put! :media-items (parse-dates (:media ALLMEDIA)))
  (session/put! :categories (:categories ALLMEDIA))
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))
