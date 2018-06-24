(ns plawww.home)

(defn home-page []

  [:div.home-page
   [:h3 "MENIU PLANETAR"]
   [:div
    [:a {:href "/media"} "Exploreaza"]]
   [:div
    [:a {:href "/media/tag/music"} "Muzica"]]
   [:div
    [:a {:href "/media/tag/radio"} "Emisiuni Radio"]]
   [:div
    [:a {:href "/media/tag/tv"} "Emisiuni TV"]]
   [:div
    [:a {:href "/media/tag/animatie"} "Animatie"]]
   [:div
    [:a {:href "/media/tag/scurtmetraj"} "Scurtmetraje"]]
   [:div
    [:a {:href "/media/tag/text"} "Carti si texte"]]
   [:div
    [:a {:href "/about"} "Despre proiect"]]])



