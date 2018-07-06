(ns plawww.texts.core
  (:require [reagent.core :as r]))

(defonce state (r/atom {:sub-menu nil}))


(defn nekrotitanium []
  [:div.book
   [:a {:href "javascript:history.back()"} ".."]
   [:h2 "NEKROTITANIUM"]
   [:p.description
    "
Din scrisorile pe care Ionel i le trimitea unchiului Bulbuc drept recunostinta pentru tratamentul din copilarie,
baba a aflat ca academicianul Koshkin a dezvoltat o serie de nanoplosnite telekinetice, capabile sa improaste cu acid orice tinta,
chiar si sub straturile de gheata din Antarctica.
Asta a convins-o pe Serafima Cozonac sa renunte la afacerea cu vata de zahar
si sa se apuce de un alt proiect: „Caprioara“.
Batrana visa deja nanoscorpioni pogramati sa vaneze caprioare, iepuri si mistreti
prin padurile Moldovei."]

   [:a {:href "/data/carti/nekrotitanium/planeta-moldova-nekrotitanium.pdf"
        :target "citeste-nekrotitanium"
        :data-title "Nekrotitanium"}
    [:img.coperta {:src "/data/carti/nekrotitanium/img/nekro_coperta.jpg"}]]])


(defn main-menu []
  (fn []
    (let [sub-menu (:sub-menu @state)]
      (case sub-menu
        :nekrotitanium
        [nekrotitanium]
        [:div.home-page
         [:h3 "TEXTELE SI CARTI"]
         [:div
          [:a {:href "/carti/nekrotitanium"} "NEKROTITANIUM"]]
         [:div
          [:a {:href "/carti/nekrotitanium"} "ALTE TEXTE"]]]))))

