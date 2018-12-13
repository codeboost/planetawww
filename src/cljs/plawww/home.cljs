;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.home
  (:require
   [plawww.paths :refer [explorer-path]]
   [reagent.core :as r])
  (:require-macros [reagent.interop :refer [$ $!]]))

(def titles
  ["MENIU"
   "MENU"
   "OFERTA"
   "PROPUNERI"
   "CE DORITI?"
   "ALEGE"
   "-----"
   "ORDINEA ZILEI"
   "OPTIUNI"
   "COMANDATI"
   "DORINTA?"
   "CE TURNAM IN CAP?"
   "ALEGE PROGRAMA"
   "YOKLMN"
   "***"
   "*"
   "?"])

(defn rand-title []
  (nth titles (rand (count titles))))

(defn menu-item [path title]
  (let [href (if (= "/" (first path)) path (explorer-path path))]
    [:div
     [:a {:href href} title]]))

(defn home-page []
  [:div.home-page
   [:h2 "PLANETA MOLDOVA"]
   [:h4 (rand-title)]
   #_[:div
      [:a {:href "/barul"} "Barul"]]
   [:div.menu-items
    [menu-item "/colectii" "Colectii"]
    [menu-item "" "Exploreaza"]
    [menu-item "tag/music" "Muzica"]
    [menu-item "tag/radio+guerilla" "Emisiuni Radio"]
    [menu-item "tag/tv" "Emisiuni TV"]
    [menu-item "tag/animatie" "Animatie"]
    [menu-item "tag/scurtmetraj" "Scurtmetraje"]
    [menu-item "/text" "Carti si texte"]
    [menu-item "/about" "Despre proiect"]
    [:div {:on-click (fn [])} "Settings"]]])

(def styles
  [{:name "Planeta Default"
    :color "#eee"
    :border "#eee"
    :background "#383838"
    :highlight "#fff"
    :dark-border "#1e1e1e"}
   {:name "Violet"
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
    :dark-border "#031e11"}])



(defn apply-style [{:keys [color background highlight border dark-border]
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




(comment
 (apply-style (get styles 2))
 (let [document-element ($ js/document :documentElement)
       style ($ document-element :style)]
   ($ style setProperty "--console-bg" "#222")))
