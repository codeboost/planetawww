(ns plawww.plamain
  (:require [clojure.string :as str]
            [clojure.core.async :refer (chan put! <!)]
            [plawww.crt :refer [crt-page]]
            [cljsjs.typedjs]
            [clojure.string :as string]))



(def EVENTCHANNEL (chan))

(def menu-back-text "INAPOI")

(defn menu-video [] {:title "Video"
                     :items [{:text "Una"}]})

(defn menu-text-carti [] {:title "Text & Carti'"
               :items [{:text "O carte"}
                       {:text "Alta carte"}]})

(defn menu-scenete [] {:title "SCENETE"
                   :items [{:text "KIDOSURI" :handler nil}
                           {:text "PEREDOAZE"}
                           {:text "GRUZURI"}
                           {:text "TAGURI"}]})

(defn main-menu [] {:title "MAIN"
                :items [{:text "SCENETE & EMISIUNI" :handler menu-scenete}
                        {:text "TV & VIDEO" :handler menu-video}
                        {:text "TEXTE & CARTI" :handler menu-text-carti}
                        {:text "CONTACT"}]})

(defonce menu-state (reagent.core/atom (main-menu)))



(defn menu-text [index text]
  (str index ". " text))

(defn back-menu-item [to-menu]
  {:text menu-back-text
   :handler to-menu})


(defn add-back-menu-item [to-menu parent-menu]
  (assoc-in to-menu [:items]
            (conj (:items to-menu) (back-menu-item parent-menu))))


;(assoc-in main-menu [:items]
;           (conj (:items main-menu)
;                 {:text "Sample"}))


(defn menu-item-clicked [menu index handler]
  (when handler
    (let [new-menu (add-back-menu-item (handler) menu)]
      (reset! menu-state new-menu))))


(defn menu-item-tag [menu index {:keys [text handler]} ]
  (let [text (menu-text index text)]
    [:li
     [:a {:href     "#"
          :on-click (fn [event]
                      (menu-item-clicked menu index handler)
                      (-> event
                          (.preventDefault)))} text]]))


;(def main-menu [["SCENETE & EMISIUNI" :scenete]
;                ["TV & VIDEO" :video]
;                ["TEXTE & CARTI" :texte]
;                ["CONTACT" :contact]])


(defn menu-items->tags [{:keys [title items] :as menu}]
  (map-indexed (fn [index menu-item]
                 (menu-item-tag main-menu index menu-item)) items))


(comment
  (menu-items->tags @menu-state))

(defn page []
  (crt-page
    [:div.plamain
     [:div "PLANETA MOLDOVA"]
     [:div "==============="]
     [:ul (menu-items->tags @menu-state)]
     [:input {:type "text"}]]))
