;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.welcome
  (:require
   [plawww.components.components :refer [typewriter]]
   [plawww.crt :refer [crt-page]]
   [reagent.core :as r]))

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
  (let [continue-visible? (r/atom false)
        skip-typing (atom nil)
        ;First click/tap anywhere skips the typing; the typewriter's
        ;on-complete shows the continue button in both cases.
        ;Document-level listener: the CRT frame overlays the content,
        ;so a handler on the content div would never receive the click.
        skip! (fn []
                (when-let [skip @skip-typing]
                  (skip)))]
    (r/create-class
     {:display-name "welcome-page"

      :component-did-mount
      (fn []
        (.addEventListener js/document "click" skip!)
        (.addEventListener js/document "touchstart" skip!))

      :component-will-unmount
      (fn []
        (.removeEventListener js/document "click" skip!)
        (.removeEventListener js/document "touchstart" skip!))

      :reagent-render
      (fn []
        [crt-page
         [:div.welcome-content
          [:p#story-text
           [typewriter {:text welcome-text
                        :speed-ms 25
                        :skip-atom skip-typing
                        :on-complete #(reset! continue-visible? true)}]]
          [:h1#continue-button.ok-button
           {:class (when @continue-visible? :visible)}
           [:a#cont-a {:href "/home"} "CONTINUARE"]]]
         {:navbar-hidden? true}])})))
