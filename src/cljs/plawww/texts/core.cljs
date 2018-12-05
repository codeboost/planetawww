(ns plawww.texts.core
  (:require
   [plawww.components.components :refer [typed-text-component]]
   [reagent.core :as r]
   [cljsjs.typedjs]))

(defonce state (r/atom {:sub-menu nil}))

(def nekro-description
  "Din scrisorile pe care Ionel i le trimitea unchiului Bulbuc drept recunostinta pentru tratamentul din copilarie, baba a aflat ca academicianul Koshkin a dezvoltat o serie de nanoplosnite telekinetice, capabile sa improaste cu acid orice tinta, chiar si sub straturile de gheata din Antarctica.
   Asta a convins-o pe Serafima Cozonac sa renunte la afacerea cu vata de zahar si sa se apuce de un alt proiect: „Caprioara“.
   Batrana visa deja nanoscorpioni pogramati sa vaneze caprioare, iepuri si mistreti prin padurile Moldovei.")

(defn nekrotitanium []
  [:div.book-container
   [:div.book
    [:a.link-back {:href "javascript:history.back()"} ".."]
    [:h2.nekro-title "NEKROTITANIUM"]
    #_[typed-text-component {:strings nekro-description
                             :showCursor false}]
    [:div.nekro-description nekro-description] ; the name, lol :)
    [:div.link-cartier
     [:a {:href "http://www.cartier.md/carti/nekrotitanium/855.html"
          :target "nekrotitanium-cartier"}
      "@ editura Cartier"]]
    [:h3.centered-text "INCEPE CALATORIA IN DIBILIZM:"]
    [:div.link-book
     [:a {:href "/data/carti/nekrotitanium/planeta-moldova-nekrotitanium.pdf"
          :target "citeste-nekrotitanium"
          :data-title "Nekrotitanium"}
      [:img.coperta {:src "/data/carti/nekrotitanium/img/nekro_coperta.jpg"}]]]]])


(defn not-yet []
  [:div.not-yet
   [:a {:href "javascript:history.back()"} ".."]
   [:h3 "Inca nu e gata."]])

(defn main-menu []
  (fn []
    (let [sub-menu (:sub-menu @state)]
      (case sub-menu
        :nekrotitanium
        [nekrotitanium]
        :altele
        [not-yet]
        [:div.home-page
         [:h3 "TEXTELE SI CARTI"]
         [:div
          [:a {:href "/carti/nekrotitanium"} "NEKROTITANIUM"]]
         [:div
          [:a {:href "/carti/altele"} "ALTE TEXTE"]]]))))

