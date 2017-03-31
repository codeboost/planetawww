;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]
            [clojure.string :as str]
            [plawww.crt :refer [crt-page]]
            [plawww.welcome :as welcome]
            [plawww.medialist :as plamain]
            [plawww.menu-page :as menu]

            [plawww.media-player :as media-player]
            [plawww.audio-player :as audio-player]
            [cljs.core.async :refer [put!]]
            [cljsjs.typedjs]))

;; -------------------------
;; Views

(defn about-page []
  [:div [:h2 "About plawww?"]
   [:div [:a {:href "/"} "go to the home page"]]])


(defn hook-up-the-stuff
  []
  (let [channel (plawww.audio-player/init)]
    (session/put! :audio-player-control-channel channel)
    (session/put! :player-state {:visible false
                                 :position 0
                                 :item {:title ""
                                        :duration 0}})
    (reagent/track! (fn []
                      (when-let [filename (session/get-in [:player-state :item :filename])]
                        (let [filename (str "/data/media/" filename)]
                          (put! channel {:command :load
                                         :filename filename
                                         :should-play true})))))))



(defn media-item-for-id [search-id]
  (first (filter (fn [{:keys [id]}]
                   (= id search-id)) (:media plamain/ALLMEDIA))))

(defn update-player-state [id]
  (when-let [media-item (media-item-for-id id)]
    (let [image-path (str "/data/images/media/" id "s.jpg")
          media-item (assoc media-item :image image-path)]
      (print "update-player-state " id)
      (session/update-in! [:player-state] merge {:position 0
                                                 :item     media-item
                                                 :visible true}))))


(defn current-page []
  (let [page (session/get :current-page)]
    (if page
      [:div [page]]
      [:div "Page does not exist."])))

(defn set-current-page
  [f]
  (session/put! :current-page f))

(defn menu-page
  [name]
  (fn []
    (print "The menu page is: " name)
    (menu/menu-page name) ))

(defn media-page
  [name]
  (fn []
    (plamain/media-page name)))


;; -------------------------
;; Routes

(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
                    (session/put! :current-page #'welcome/page))

(secretary/defroute "/about" []
                    (session/put! :current-page #'about-page))

(secretary/defroute "/menu/" [] (set-current-page (menu-page "main")))
(secretary/defroute "/menu" [] (set-current-page (menu-page "main")))

(secretary/defroute menu-path "/menu/:menu-name" {menu-name :menu-name}
                    (set-current-page (menu-page menu-name)))

(secretary/defroute "/media/" [q]
                    (set-current-page (media-page q)))

;(secretary/defroute #"/media/([a-z]+)" [tag]
;                    (set-current-page (media-page tag)))

;(secretary/defroute #"/media/(\d+)" [id]
(secretary/defroute #"/media/(\d+)" [id q]
                    (set-current-page (media-page q))
                    (update-player-state (js/parseInt id)))

;(secretary/locate-route "/menu/")

;; -------------------------
;; Initialize app

(defn mount-root []
  (welcome/on-init)
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root)
  (hook-up-the-stuff))