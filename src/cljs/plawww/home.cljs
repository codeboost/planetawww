;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.home
  (:require
   [plawww.paths :refer [explorer-path tags-path]]
   [plawww.themes.themes :as themes]
   [reagent.core :as r]))

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
  (let [visible-dialog (r/atom :none)]
    (fn []
      [:div.home-page
       [:h2 "PLANETA MOLDOVA"]
       [:h4 (rand-title)]
       #_[:div
          [:a {:href "/barul"} "Barul"]]
       [:div.menu-items
        [menu-item "/colectii" "Colectii"]
        [menu-item "" "Exploreaza"]
        [menu-item (tags-path "music") "Muzica"]
        [menu-item (tags-path "radio+guerilla") "Emisiuni Radio"]
        [menu-item (tags-path "tv") "Emisiuni TV"]
        [menu-item (tags-path "animatie") "Animatie"]
        [menu-item (tags-path "scurtmetraj") "Scurtmetraje"]
        [menu-item "/text" "Carti si texte"]
        [menu-item "/despre" "Despre proiect"]
        [:a {:on-click #(reset! visible-dialog :theme-picker)
             :href :#}
         "Culoare"]
        (case @visible-dialog
          :theme-picker
          [themes/theme-picker-component {:on-close #(reset! visible-dialog :none)}]
          nil)
        #_[themes/theme-picker-component]]])))
