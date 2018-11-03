;   Copyright (c) Braghis Florin. All rights reserved.
;   Released under GPL-3.0 license
;   https://opensource.org/licenses/GPL-3.0.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.navbar.search-component
  (:require [clojure.string :as str]
            [reagent.core :as r]
            [reagent.session :as session]))

(defn- search-input
  "Renders the search input box"
  [search-string on-change]
  [:input.search-box {:type :text
                      :on-change on-change
                      :value search-string}])


(defn- censor-strings
  "Given a collection of strings `coll`, returns a new collection without the strings that start with 'xx '.
  Basically, if you want "
  [coll xx?]
  (if xx?
    (mapv #(str/replace % "xx " "") coll)
    (filterv #(not (str/starts-with? % "xx ")) coll)))


(def PROMPTS ["CE DORITI?"
              "CU CE VA PUTEM SERVI?"
              "SRCH:"
              "CAUT:"
              "SI VREI?"
              "PRAPADIT?"
              "TOARNA:"
              "SHOPTESHTE.."
              "AMNEZIE?"
              "VEI GASI:"
              "PROBLEME?"
              "GADILITURA NEURONALA:"
              "OBSESIVCOMPULSIVITATEA:"
              "CUM SE NUMEA PRAGONUL?"
              "YOKLMN MLEA!"
              "VREU'S'ASCULT:"
              "DENUMIREA DORINTEI:"
              "CINE CAUTA GASESTE:"
              "CAUTA SI GASESTE!"
              "CAUTATI, CAUTATI, CAUTATI:"
              "NU CUMVA SA SCRII AICI CEVA!"
              "DENUMIREA PRAGONULUI:"
              "SCRIE SI VREI"
              "CE-I SCRIS CU BARDITZA, NU TAI CU PENIZA:"
              "CRIME, DROGURI SAU DRAGOSTE ?"
              "xx KIZDOI"])

(defn random-search-prompt [xx?]
  (let [prompts (censor-strings PROMPTS xx?)
        index (rand-int (count prompts))]
    (nth prompts index)))

(defn random-not-found-msg [xx?]
  (let [emotions ["GRUZ:"
                  "HRENOVO:"
                  "MARI JELI:"
                  "NU RETRAITI, TOTUL VA FI BINE CANDVA, DAR ACUM:"
                  "DEAMU SNISKUZATI CUM S-AR ZICE, DAR"
                  "PROCESORUL ISI CERE SCUZE:"
                  "NIPAVERISH - "
                  "NOUTATE EXTREM DE REA:"
                  "EROARE EXTREMALA:"
                  "FATAL TOTAL ABSOLUT ZERO INFINIT ! "
                  "ABSOLUT (VADIARA):"
                  "M-AI USHIS:"
                  "PEREDOZ DE CURIOZITATE:"
                  "xx VAFLI: "
                  "xx DA NU NAHUI BLEA:"
                  "xx CAROCE ASA HUINEA: "
                  "xx HUIOVO PIZDET:"
                  "xx DE-A PULA:"]

        messages  ["GOLEAK REZULTATE"
                   "NU'I ASA CEVA, NA!"
                   "CANESHNA C'NUI!"
                   "BREDITZI, STIMABILE ?"
                   "MATINKA TE-AI INCURCAT"
                   "xx NISH O PULA N-AM GASIT"
                   "xx MULTIMEA DE REZULTATE ESTE GOALA CA KIZDA RASHKIRATA."]]



    (let [emotions (censor-strings emotions xx?)
          messages (censor-strings messages xx?)
          emotion (emotions (rand-int (count emotions)))
          message (messages (rand-int (count messages)))]
      [:span.no-results
       [:span.emotion emotion]
       " "
       [:span.message message]])))

(defn search-component [{:keys [search-string on-change]}]
  [:div.search-component
   [:div.search-control
    [:div.search-text (random-search-prompt (session/get :xx?))]
    [search-input search-string on-change]]])


