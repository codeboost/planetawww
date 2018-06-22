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
   [plawww.media-player.core :as media-player]
   [plawww.media-player.audio-player :as audio-player]
   [plawww.paths :as paths]
   [plawww.puzzle :as puzzle]
   [reagent.core :as reagent :refer [atom]]
   [reagent.session :as session]
   [secretary.core :as secretary :refer [defroute]]))


(defonce ALLMEDIA (:media (js->clj js/kolbasulPlanetar :keywordize-keys true)))

;; -------------------------
;; Views

(defn about-page []
  [:div [:h2 "About plawww?"]
   [:div [:a {:href "/"} "go to the home page"]]])


(defn hook-up-the-stuff
  []
  (session/put! :media-items ALLMEDIA)
  (session/put! :player-state {:visible false
                               :detail-visible? false
                               :position 0
                               :volume 0.6
                               :should-show-detail? true
                               :item {:title ""
                                      :duration 0}})

  (let [channel (audio-player/init)]
    (session/put! :audio-player-control-channel channel)

    (reagent/track!
     (fn []
       (when-let [filename (session/get-in [:player-state :item :filename])]
         (let [filename (paths/media-path filename)]
           (put! channel {:command :load
                          :filename filename
                          :should-play true})))))))


(defn media-item-for-id [search-id]
  (first (filter (fn [{:keys [id]}]
                   (= id search-id)) ALLMEDIA)))


(defn with-item-image [item])

(defn set-current-media-item
  "Updates the current media item in the session-state, which should trigger the media player to show and load the corresponding media.
  If :should-show-detail? is true, detail-visible? is set to true (which opens the player in detail mode).
  This only happens once, if user decides to hide details, the player will stay closed when a new item is loaded.
  This way, the user gets control of the detail showing, allowing him to toggle it.
  "
  [id]
  (when-let [media-item (media-item-for-id id)]
    (let [image-path (paths/s-image-path id)
          media-item (assoc media-item :image image-path)
          should-show-detail? (or
                               (session/get-in [:player-state :should-show-detail?])
                               (session/get-in [:player-state :detail-visible?]))]
      (session/update-in! [:player-state] merge {:position 0
                                                 :item     media-item
                                                 :visible true
                                                 :detail-visible? should-show-detail?})

      (media-page/set-opts {:selected-id id})
      (when should-show-detail?
        (session/update-in! [:player-state] dissoc :should-show-detail?)))))

(defn- media-browser-page []
  [crt-page
   [media-page/media-page ALLMEDIA]])

(defn- show-media-browser [& [opts :or {}]]
  (js/console.log "show-media-browser: " opts)
  (session/put! :current-page #'media-browser-page)
  (media-page/set-opts opts))

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
          (set-current-media-item (js/parseInt id)))

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
