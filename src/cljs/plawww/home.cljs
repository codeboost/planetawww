(ns plawww.home)

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

(defn home-page []
  [:div.home-page
   [:h1 "PLANETA MOLDOVA"]
   [:h3 (rand-title)]
   #_[:div
      [:a {:href "/barul"} "Barul"]]
   [:div
    [:a {:href "/explorer"} "Exploreaza"]]
   [:div
    [:a {:href "/explorer/tag/music"} "Muzica"]]
   [:div
    [:a {:href "/explorer/tag/radio+guerilla"} "Emisiuni Radio"]]
   [:div
    [:a {:href "/explorer/tag/tv"} "Emisiuni TV"]]
   [:div
    [:a {:href "/explorer/tag/animatie"} "Animatie"]]
   [:div
    [:a {:href "/explorer/tag/scurtmetraj"} "Scurtmetraje"]]
   [:div
    [:a {:href "/text"} "Carti si texte"]]
   [:div
    [:a {:href "/about"} "Despre proiect"]]])



