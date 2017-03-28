(ns plawww.text-menu-component
  (:require [clojure.string :as str]
            [reagent.core :as r]))

(def *show-indexes* false)
(def menu-back-text "INAPOI")

(defn menu-text [index text]
  "Returns a formatted menu item with the index prepended.
  `index` is 0-based, but will be rendered as 1-based (eg. increased by 1)"
  (str (when *show-indexes* (str (inc index) ". ")) text))

(defn- back-menu-item [index]
  "Generic back menu item"
  ^{:key 999999} [:li [:a {:href "javascript:history.go(-1);"} (menu-text index menu-back-text)]])

(defn- menu-item-tag [index {:keys [text handler id]}]
  "Menu item to hiccup."
  (let [text (menu-text index text)
        href (if handler handler "/menu/")]
    ^{:key index} [:li [:a {:href href} text]]))

(defn title-with-count [title count]
  (str title ": " count))

(defn menu->hiccup [{:keys [title items] :as menu} show-back?]
  "Renders a menu and its items"
  (let [show-items? (r/atom false)
        num-items (count items)]
    (fn []
      [:div.menu
       [:div.title [:a {:on-click (fn [] (reset! show-items? (not @show-items?)))
                        :class (if @show-items? "opened" "")}
                    (title-with-count title num-items)]]
       (when @show-items?
         (when (pos? (count items))
           (let [list-items (vec (map-indexed menu-item-tag items))
                 list-items (if show-back?
                              (conj list-items (back-menu-item (count items)))
                              list-items)]
             (into [:ul.items] list-items))))])))

(comment
  ;Sample menu
  (def MENUS {
              :main {:title "MAIN"
                     :items [{:text "SCENETE & EMISIUNI" :handler "/menu/scenete"}
                             {:text "TV & VIDEO" :handler "/menu/video"}
                             {:text "TEXTE & CARTI" :handler "/menu/carti"}
                             {:text "CONTACT" :handler "/menu/scontact"}]}

              :scenete {:title "SCENETE"
                        :items [{:text "KIDOSURI" :handler "/media/kidosuri"}
                                {:text "PEREDOAZE" :handler "/media/peredoaze"}
                                {:text "GRUZURI" :handler "/media/gruzuri"}
                                {:text "TAGURI" :handler "media/tags"}]}
              :carti {:title "Text & Carti'"
                      :items [{:text "O carte"}
                              {:text "Alta carte"}]}
              :video {:title "Video"
                      :items [{:text "Una"}]}
              }))