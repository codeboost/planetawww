(ns plawww.menu-page
  (:require [plawww.crt :refer [crt-page]]
            [plawww.text-menu-component :refer [menu->hiccup]]))


(def MENUS {
            :main {:title "MAIN"
                   :items [{:text "SCENETE & EMISIUNI" :handler "/media/scenete"}
                           {:text "ANIMATII " :handler "/menu/animatii"}
                           {:text "TEXTE & CARTI" :handler "/menu/carti"}
                           {:text "CONTACT" :handler "/menu/scontact"}]}

            :carti {:title "Text & Carti'"
                    :items [{:text "O carte"}
                            {:text "Alta carte"}]}
            :video {:title "Video"
                    :items [{:text "Una"}]}
            })

(defn render-menu [name show-back?]
  (let [menu (or (MENUS (keyword name)) (MENUS :main))]
    [menu->hiccup menu show-back?]))

(defn menu-page [menu-name]
  (print "Menu name: " menu-name)
  (crt-page
    [:div.plamain
     [:div "PLANETA MOLDOVA"]
     [:div "==============="]
     (render-menu  menu-name (not (nil? menu-name)))]))