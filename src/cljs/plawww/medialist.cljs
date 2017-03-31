;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.medialist
  (:require [clojure.string :as str]
            [clojure.core.async :refer (chan put! <!)]
            [plawww.crt :refer [crt-page]]
            [cljsjs.typedjs]
            [clojure.string :as str]
            [reagent.core :as r]
            [plawww.search-component :refer [search-component]]
            [plawww.text-menu-component :refer [menu->hiccup]]))


(defonce ALLMEDIA (js->clj js/kolbasulPlanetar :keywordize-keys true))

(def default-options {:group-by      :tag
                      :display       :list
                      :dirty         true
                      :search-string ""
                      :tags          #{}})


(comment
  (assoc default-options :tags (conj (:tags default-options) "tag"))
  )

(defonce *display-options* (r/atom default-options))

(defn media->menu-items [media-items]
  (let [result (map (fn [{:keys [title id]}]
                      {:id      id
                       :text    title
                       :handler (str "/media/" id)}) media-items)]
    result))


(defn items-for-tag [media-items tag]
  "Returns items from media-items which contain the tag `tag`."
  (filter (fn [{:keys [tags]}]
            (some (fn [a-tag]
                    (= a-tag tag)) tags)) media-items))

(defn tags-from-items
  "Returns a set of unique tags extracted from the media items."
  [media-items]
  (->> media-items
       (map (fn [{:keys [tags]}] tags))
       (apply concat)
       (map str/trim)
       (set)))


(defn in? [coll el]
  (some #(= el %) coll))

(defn expand-tag? [tag tags]
  (and (pos? (count tags))
       (contains? tags tag)))


(defn by-tags [media-items]
  "Returns a list of maps with following keys: [:tag :items :num-items]"
  (let [tags (tags-from-items media-items)]
    (reverse
      (sort-by
        :num-items
        (map (fn [tag]
               (let [items (items-for-tag media-items tag)
                     num-items (count items)
                     items items]
                 {:title     tag
                  :items     items
                  :num-items num-items})) tags)))))

(defn toggle-tag [tags tag]
  (let [setfn (if (contains? tags tag) disj conj)
        new-tags (setfn tags tag)]
    new-tags))

(defn title-with-count [title count]
  (let [title (if (str/blank? title) "*" title)]
    (str title ": " count)))



(comment
  (update-in! *display-options* [:tags] set #{"hello"})
  (swap! *display-options* assoc :tags #{"hello"})

  (swap! *display-options* dissoc :dissoc)

  )

(defn track-expanded-tags [expanded-atom tag opts]
  (r/track! (fn []
              (let [is-expanded? @expanded-atom
                    tags (:tags @opts)
                    new-tags (if is-expanded? (conj tags tag) (disj tags tag))]
                (when-not (= tags new-tags)
                  (do
                    (swap! *display-options* assoc :tags new-tags)
                    (print "Tags:" new-tags)))))))

(defn group->menu [{:keys [title items num-items]}]
  (let [expanded? (r/atom false)
        track (track-expanded-tags expanded? title *display-options*)
        menu {:title title
              :items (media->menu-items items)}]
    [menu->hiccup menu expanded?]))

(defn render-by-tags [media-items opts]
  (let [tagged (by-tags media-items)
        menus (map group->menu tagged)]
    (into [:div.media-items.horiz-container] menus)))

;--------- BY LETTER ----------------------

(defn grouped-by-first-letter [media-items]
  (into (sorted-map) (group-by (fn [{:keys [title]}]
                                 (str/upper-case (first title))) media-items)))

(defn render-by-letter [media-items]
  (let [grouped (grouped-by-first-letter media-items)
        results (map (fn [[first-letter item]]
                       (group->menu item)) grouped)]
    (into [:div.media-items.horiz-container] results)))

(defn random-search-prompt []
  (let [prompts ["CE DORITI?"
                 "CU CE VA PUTEM SERVI?"
                 "SRCH:"
                 "CAUT:"
                 "SI VREI?"]
        index (rand-int (count prompts))]
    (nth prompts index)))


(defn search-in-items [media-items search-string]
  (filter (fn [{:keys [title]}]
            (or (str/blank? search-string)
                (not= -1 (.indexOf
                           (str/lower-case title)
                           (str/lower-case search-string))))) media-items))

(defn media-items-component [items opts]
  (let [{:keys [search-string group-by]} @opts
        filtered-items (search-in-items items search-string)]
    (cond
      (= group-by :tag) [render-by-tags filtered-items opts]
      (= group-by :plain) [render-by-letter filtered-items])))

(defn media-page [opts]
  (let [opts *display-options*
        search-prompt (random-search-prompt)]
    (fn []
      [crt-page
       [:div.media-page
        [:h4.page-title "PLANETA MOLDOVA"]
        [search-component search-prompt opts]
        [:div.v16px]
        [:div.page-content
         (let [items (:media ALLMEDIA)]
           [media-items-component items opts])]]])))

;------------------------------------------------------------------------



(comment

  (by-tags (take 1 (:media ALLMEDIA)) [])

  (render-by-letter (:media ALLMEDIA))
  (group-by (fn [{:keys [title]}]
              (str/upper-case (first title))) (:media ALLMEDIA)))

(comment
  (let [the-atom])
  (render-media-items (take 1 (:media ALLMEDIA)) default-options)
  (render-all-media (take 100 (:media ALLMEDIA)))
  )



