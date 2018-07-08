(ns plawww.about.core
  (:require [plawww.components.core :refer [typed-text-component]]))

(def about-text
  "
  Acest site a fost pus la kale de Planeta Moldova (ex. 2P-Trip), cu multa grija si nostalgie neimpacata...<br>
  De la mare distanta, cu multa rabdare si daruire, acest proiect a fost conceput pentru detasarea
  spatial-temporala si neuro-transcendentala a sufletelor dornice de senzatii tari, umor negru, mat-peremat,
  haz de necaz, ubiveala, pragoane, kidosuri, gruzuri si peredoaze. Declaram solidaritatea si respectul nostru
  neconditionat, fata de toate paturile sociale, de la gospodari, muncitori, intelectuali, patrioti si artisti,
  pana la sineaci, narcomani, debilomani, cersetori si puscariasi. Dedicam acest site, tuturor prietenilor nostri
  din Moldova, precum si celor din 'Exil'.<br>
  Cu sufletism dezekilibrat si dor neimpacat, de dupa Cordon, ai vostri Planeta Moldova.
  ")


(defn page []
  [:div.about-page
   [:h3.centered-text "DESPRE PLANETA MOLDOVA"]
   [:div.centered-text "-----"]
   [typed-text-component {:strings [about-text]
                          :showCursor false}]])