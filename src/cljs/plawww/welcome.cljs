;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.welcome
  (:require
   [plawww.crt :refer [crt-page]]
   [cljsjs.typedjs]))

(def welcome-text "Anul 3000.
Bespredelu', marazmu' si gruzul au crescut considerabil in Moldova.

Tara s-a micsorat, au mai aparut inca 5 limbi de stat, pe langa cele 5 oficiale. Oamenii au uitat cine sunt, de unde vin si unde se duc.

Guvernul secret al Moldovei \"Lacrima lui Stefan\" a hotarat sa ia masuri.
Tara a fost data in arenda, iar pe banii obtinuti a fost construita o nava cosmica gigantica 'Arca Noastra' pe care
s-a imbarcat poporul Moldovenesc si cu lacrimi in ochi a parasit Pamantul.

Peste 100 de ani, moldovenii au ajuns in alta galaxie, pe o planeta libera si frumoasa, unde s-au stabilit definitiv.
Inca peste 100 de ani Moldovenii au ajuns la un nivel de trai atat de avansat, incat au atras si alte popoare gruzanite de pe Pamant.

In scurt timp toate tarile de pe Pamant s-au refugiat in spatiu, au cerut azil politic si s-au stabilit pe infloritoarea planeta.
")

(defn page []
  [crt-page
    [:div.welcome-content
     [:p#story-text]
     [:h1#continue-button.ok-button
      [:a#cont-a {:href "/home"} "CONTINUARE"]]]
    {:navbar-hidden? true}])

(defn continue-on-click[]
  (-> (js/$ "body")
      (.on "click touchstart" (fn[]
                               (-> (js/jQuery "#continue-button")
                                   (.fadeIn 1000))))))

(defn on-init[]
  (js/$ (fn []
          (continue-on-click)
          (->
            (js/$ "#story-text")
            (.typed (clj->js {:strings [welcome-text]
                              :showCursor false}))))))