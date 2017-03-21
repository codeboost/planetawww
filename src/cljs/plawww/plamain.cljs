(ns plawww.plamain
  (:require [clojure.string :as str]
            [clojure.core.async :refer (chan put! <!)]
            [plawww.crt :refer [crt-page]]
            [cljsjs.typedjs]
            [clojure.string :as string]
            [reagent.core :as r]
            [clojure.string :as s]
            [plawww.search-component :refer [search-component]]
            [plawww.text-menu-component :refer [menu->tags]]))

(defonce ALLMEDIA (js->clj js/kolbasulPlanetar :keywordize-keys true))

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
            })

(defn media->menu-items[media-items]
  (let [result (mapv (fn[{:keys [title id]}]
                {:id id
                 :text title
                 :handler (str "/media/" id)}) media-items)]
    result))

(defn render-menu [name show-back?]
  (let [menu (or (MENUS (keyword name)) (MENUS :main))]
    (menu->tags menu show-back?)))

(defn render-media-menu [title media-items]
  (let [menu {:title title
              :items (media->menu-items media-items)}]
  (menu->tags menu false)))

(defn render-all-media [media]
  (map (fn [part]
         (render-media-menu "PRAGOANELE" part))
       (partition 20 media)))

(defn items-for-tag [media-items tag]
  "Returns items from media-items which contain the tag `tag`."
  (vec (filter (fn [{:keys [tags]}]
                 (some (fn [a-tag]
                         (= a-tag tag)) tags)) media-items)))

(defn tags-from-items [media-items]
  (let [tags (map (fn [{:keys [tags]}] tags) media-items)
        tags (apply concat tags)
        tags (map str/trim tags)
        tags (set tags)]
    tags))

(defn by-tags [media-items]
  "Returns a list of maps which contain the tag-name, the media items that contain that tag."
  (let [tags (tags-from-items media-items)]
    (reverse
      (sort-by :count
               (map (fn [tag]
                      (let [items (items-for-tag media-items tag)]
                        {:tag   tag
                         :items items
                         :count (count items)})) tags)))))

(defn render-by-tags [media-items]
  (let [tagged (by-tags media-items)
        results
        (map (fn [{:keys [tag items]}]
               (render-media-menu tag items)) tagged)]
    results))

(comment
  (render-all-media (:media ALLMEDIA))
  (render-by-tags (take 1 (:media ALLMEDIA)))
  (->> (:media ALLMEDIA)
       (take 10)
       (by-tags))
  )

(defn random-search-prompt []
  (let [prompts ["CE DORITI?"
                 "CU CE VA PUTEM SERVI?"
                 "SRCH:"
                 "CAUT:"
                 "SI VREI?"]
        index (rand-int (count prompts))]
    (nth prompts index)))

(def default-search-settings {:group-by :tag
                              :display :list
                              :dirty true
                              :search-string ""})

(defn media-page [filter-name]
  (let [search-settings (r/atom default-search-settings)
        search-prompt (random-search-prompt)]
    (fn []
      (crt-page
        [:div.media-page
         [:h2.page-title "PLANETA MOLDOVA"]
         [search-component search-prompt search-settings]
         [:div.v16px]
         [:div.media-items.horiz-container
          (let [search-string (:search-string @search-settings)]
            (render-by-tags (filter (fn [{:keys [title]}]
                                      (or (str/blank? search-string)
                                          (str/starts-with?
                                            (str/lower-case title)
                                            (str/lower-case search-string)))) (:media ALLMEDIA))))]]))))

(defn menu-page [menu-name]
  (print "Menu name: " menu-name)
  (crt-page
    [:div.plamain
     [:div "PLANETA MOLDOVA"]
     [:div "==============="]
     (render-menu  menu-name (not (nil? menu-name)))]))

