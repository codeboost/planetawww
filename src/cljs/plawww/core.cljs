(ns plawww.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]
            [clojure.string :as str]
            [plawww.crt :refer [crt-page]]
            [plawww.welcome :as welcome]
            [plawww.plamain :as plamain]
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
    (reagent/track! (fn []
                      (when-let [filename (session/get-in [:player-state :item :filename])]
                        (let [filename (str "/data/media/" filename)]
                          (put! channel {:command :load
                                         :filename filename
                                         :should-play true})))))))

(defn update-player-state [id]
  (when-let [media-item (plawww.plamain/media-item-for-id id)]
    (let [image-path (str "/data/images/media/" id "s.jpg")
          media-item (assoc media-item :image image-path)]
      (print "update-player-state " id)
      (session/update-in! [:player-state] merge {:position 0
                                                 :item     media-item}))))


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
    (plamain/menu-page name) ))

(defn media-page
  [name]
  (fn []
    (plamain/media-page "")))


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

(secretary/defroute "/media/" []
                    (set-current-page (media-page "")))

(secretary/defroute "/media/:id" {id :id}
                    (do
                      (print "Route hit: /media/" id)
                      (session/put! :current-page (fn [] (plamain/media-page id)))
                        (update-player-state (js/parseInt id))))

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