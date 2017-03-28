(ns plawww.plamain
  (:require [clojure.string :as str]
            [clojure.core.async :refer (chan put! <!)]
            [plawww.crt :refer [crt-page]]
            [cljsjs.typedjs]
            [clojure.string :as string]
            [reagent.core :as r]
            [clojure.string :as s]
            [plawww.search-component :refer [search-component]]
            [plawww.text-menu-component :refer [menu->hiccup]]))

(defonce ALLMEDIA (js->clj js/kolbasulPlanetar :keywordize-keys true))

(defn media-item-for-id [search-id]
  (first (filter (fn [{:keys [id]}]
                   (= id search-id)) (:media ALLMEDIA))))

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

(defn media->menu-items[media-items]
  (let [result (map (fn[{:keys [title id]}]
                {:id id
                 :text title
                 :handler (str "/media/" id)}) media-items)]
    result))

(defn render-menu [name show-back?]
  (let [menu (or (MENUS (keyword name)) (MENUS :main))]
    [menu->hiccup menu show-back?]))

(defn render-media-menu [title media-items]
  (let [menu {:title title
              :items (media->menu-items media-items)}]
  [menu->hiccup menu false]))


(defn render-all-media [media]
  (into [:div.media-items.horiz-container] (map (fn [part]
         (render-media-menu "PRAGOANELE" part))
       (partition 20 media))))

(defn items-for-tag [media-items tag]
  "Returns items from media-items which contain the tag `tag`."
  (filter (fn [{:keys [tags]}]
            (some (fn [a-tag]
                    (= a-tag tag)) tags)) media-items))

(defn tags-from-items [media-items]
  (->> media-items
       (map (fn [{:keys [tags]}] tags))
       (apply concat)
       (map str/trim)
       (set)))

(comment
  (group-by (fn [{:keys [title]}]
              (str/upper-case (first title))) (:media ALLMEDIA))
  )

(defn by-tags [media-items]
  "Returns a list of maps with following keys: [:tag :items :count]"
  (let [tags (tags-from-items media-items)]
    (reverse
      (sort-by
        :count
        (map (fn [tag]
               (let [items (items-for-tag media-items tag)]
                 {:tag   tag
                  :items items
                  :count (count items)})) tags)))))


(defn tags->menus
  "Creates the menus for the items grouped by tags."
  [items-by-tag]
  (map (fn [{:keys [tag items]}]
         (let  [tag (if (str/blank? tag) "diverse" tag)]
           (render-media-menu tag items))) items-by-tag))

(defn render-by-tags [media-items]
  (let [tagged (by-tags media-items)
        menus (tags->menus tagged)]
    (into [:div.media-items.horiz-container] menus)))


(defn grouped-by-first-letter [media-items]
  (into (sorted-map) (group-by (fn [{:keys [title]}]
                          (str/upper-case (first title))) media-items)))

(defn render-by-letter [media-items]
  (let [grouped (grouped-by-first-letter media-items)
        results (map (fn[[first-letter item]]
                       (render-media-menu first-letter item)) grouped)]
    (into [:div.media-items.horiz-container] results)))

(comment
  (render-by-letter (:media ALLMEDIA))
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


(defn search-in-items [media-items search-string]
  (filter (fn [{:keys [title]}]
            (or (str/blank? search-string)
                (not= -1 (.indexOf
                           (str/lower-case title)
                           (str/lower-case search-string))))) media-items))


(defn media-items-component [media-items search-settings]
  (let [search-string (:search-string search-settings)
        group-by (:group-by search-settings)
        filtered-items (search-in-items media-items search-string)]

    (cond
      (= group-by :tag) (render-by-tags filtered-items)
      (= group-by :plain) (render-by-letter filtered-items))))

(defn media-page [filter-name]
  (let [search-settings (r/atom default-search-settings)
        search-prompt (random-search-prompt)]
    (fn []
      [crt-page
        [:div.media-page
         [:h4.page-title "PLANETA MOLDOVA"]
         [search-component search-prompt search-settings]
         [:div.v16px]
         [:div.page-content
          [media-items-component (:media ALLMEDIA) @search-settings]]]])))

(comment
  (let [the-atom ])
  (render-media-items (take 1 (:media ALLMEDIA)) default-search-settings)
  (render-all-media (take 100 (:media ALLMEDIA)))
  )


(defn menu-page [menu-name]
  (print "Menu name: " menu-name)
  (crt-page
    [:div.plamain
     [:div "PLANETA MOLDOVA"]
     [:div "==============="]
     (render-menu  menu-name (not (nil? menu-name)))]))

