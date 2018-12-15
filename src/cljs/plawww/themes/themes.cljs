(ns plawww.themes.themes
  (:require
   [plawww.ui :as ui]
   [reagent.core :as r])
  (:require-macros [reagent.interop :refer [$ $!]]))

(def themes
  [{:name "Planeta"
    :color "#eee"
    :border "#eee"
    :background "#383838"
    :highlight "#fff"
    :dark-border "#1e1e1e"}
   {:name "Fiolet"
    :color "#8580BB"
    :border "#8580BB"
    :background "#27226D"
    :highlight "#fff"
    :dark-border "#1e1e1e"}
   {:name "CRT"
    :color "#14fdce"
    :border "#14fdce"
    :background "#031e11"
    :highlight "#fff"
    :dark-border "#031e11"}
   {:name "TRI"
    :color "yellow"
    :background "blue"
    :highlight "#EFC3E6"
    :border "red"}
   {:name "CAF"
    :color "#22223B"
    :background "#F2E9E4"
    :highlight "#EFC3E6"
    :border "#4A4E69"}
   {:name "AN"
    :color "black"
    :background "white"
    :highlight "white"
    :border "black"}
   {:name "NA"
    :color "white"
    :background "black"
    :highlight "white"
    :border "white"}])




(defn apply-theme [{:keys [color background highlight border dark-border]
                    :or {highlight color
                         border color
                         dark-border background}}]
  (let [document-element ($ js/document :documentElement)
        style ($ document-element :style)]
    ($ style setProperty "--console-bg" background)
    ($ style setProperty "--console-color" color)
    ($ style setProperty "--border-color" border)
    ($ style setProperty "--highlight-color" highlight)
    ($ style setProperty "--dark-border-color" dark-border)))


(defn theme-component [{:keys [name color background border]
                        :or {border color}
                        :as theme}]
  [:div.theme
   {:style {:background-color background
            :color color
            :border (str "2px solid " border)}}
   [:div {:on-click #(apply-theme theme)} name]])



(defn theme-picker-component [{:keys [on-close]}]
  [ui/modal-dialog
   {:dialog-class :theme-picker-dialog
    :on-close on-close}
   [:div.theme-picker
    [:h3.title "Alege"]
    (into
     [:div.themes]
     (mapv theme-component themes))]])



(comment
 (apply-style (get styles 2))
 (let [document-element ($ js/document :documentElement)
       style ($ document-element :style)]
   ($ style setProperty "--console-bg" "#222")))
