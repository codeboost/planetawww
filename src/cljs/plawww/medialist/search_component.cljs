;   Copyright (c) Braghis Florin. All rights reserved.
;   Released under GPL-3.0 license
;   https://opensource.org/licenses/GPL-3.0.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.medialist.search-component
  (:require [clojure.string :as str]
            [reagent.core :as r]))

(defn- search-input
  "Renders the search input box and swaps the :search-string "
  [settings-atom keyname]
  [:input.search-box {:type      "text"
                      :on-change #(swap! settings-atom conj {keyname (-> % .-target .-value)})
                      :value (get @settings-atom keyname)}])

(defn- toggle-button [props text]
  [:a.toggle-button props text])

(defn- button-all [*state]
  (fn []
    [toggle-button {:on-click #(swap! *state update-in [:show-all?] not)
                    :class-name (when (:show-all? @*state) :on)} "TOT"]))

(defn- button-tags [selected?]
  [toggle-button
   {:class-name (if selected? :on "")
    :href "/media/tag"} "TAG"])

(defn- button-letters [selected?]
  [toggle-button
   {:class-name (if selected? :on "")
    :href "/media/letter"} "LIT"])

(def av-states ["AV" "A." ".V"])

(defn button-av-handler [*state]
  (swap! *state update-in [:av] #(mod (inc %) (count av-states))))

(defn- button-av [*state]
  (let [i (int (:av @*state))
        title (nth av-states i)
        handler (fn [_] (swap! *state update-in [:av] #(mod (inc %) (count av-states))))]
      [toggle-button
       {:on-click handler
        :class-name (when (pos? i) :on)}
       title]))

(defn- button-item-details [*state]
  (fn []
    (let [detail-items? (:detail-items? @*state)]
      [toggle-button    {:on-click #(swap! *state update-in [:detail-items?] not)
                         :class-name (when detail-items? :on)}
       "DET"])))

(defn- search-component-filters [*state]
  (fn []
    (let [searching? (pos? (count (:search-string @*state)))]
      (when-not searching?
        [:div.filters
         [button-all *state]
         [button-av *state]
         [button-item-details *state]
         [button-tags (= :tag (:group-by @*state))]
         [button-letters (= :plain (:group-by @*state))]]))))

(defn random-search-prompt []
  (let [prompts ["CE DORITI?"
                 "CU CE VA PUTEM SERVI?"
                 "SRCH:"
                 "CAUT:"
                 "SI VREI?"
                 "PRAPADIT?"
                 "TOARNA:"
                 "SHOPTESHTE:"
                 "AMNEZIE?"
                 "VEI GASI:"
                 "PROBLEME?"]
        index (rand-int (count prompts))]
    (nth prompts index)))


(defn random-not-found-msg []
  (let [emotions ["GRUZ:"
                  "HRENOVO:"
                  "MARI JELI:"
                  "NU RETRAITI, TOTUL VA FI BINE CANDVA, DAR ACUM:"
                  "DEAMU SNISKUZATI CUM S-AR ZICE, DAR"]
        messages  ["GOLEAK REZULTATE"
                   "N-AVEM ASA CEVA"
                   "CANESHNA C'NUI!"
                   "BREDITZI, STIMABILE ?"
                   "MATINKA TE-AI INCURCAT"]]
    (let [emotion (emotions (rand-int (count emotions)))
          message (messages (rand-int (count messages)))]
      [:span.no-results
       [:span.emotion emotion]
       " "
       [:span.message message]])))

(defn search-component [*state]
  [:div.search-component
   [:div.search-text (random-search-prompt)]
   [search-input *state :search-string]
   [search-component-filters *state]])

