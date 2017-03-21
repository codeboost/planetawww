(ns plawww.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]
            [clojure.string :as str]
            [plawww.crt :refer [crt-page]]
            [plawww.welcome :as welcome]
            [plawww.plamain :as plamain]
            [cljsjs.typedjs]))

;; -------------------------
;; Views

(defn about-page []
  [:div [:h2 "About plawww?"]
   [:div [:a {:href "/"} "go to the home page"]]])



(secretary/set-config! :prefix "#")

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes

(secretary/defroute "/" []
  (session/put! :current-page #'welcome/page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))


(secretary/defroute "/menu/" []
                    (session/put! :current-page (fn[] (plamain/menu-page "main"))))

(secretary/defroute "/menu/:menu-name" {menu-name :menu-name}
                    (session/put! :current-page (fn[] (plamain/menu-page menu-name))))


(secretary/defroute "/media/" []
                    (session/put! :current-page (fn[] (plamain/media-page ""))))

(secretary/defroute "/media/:id" {id :id}
                    (session/put! :current-page (fn[] (plamain/media-page id))))

(secretary/locate-route "/menu/")

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
  (mount-root))
