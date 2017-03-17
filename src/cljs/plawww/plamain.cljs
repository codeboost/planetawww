(ns plawww.plamain
  (:require [clojure.string :as str]
            [plawww.crt :refer [crt-page]]
            [cljsjs.typedjs]
            [clojure.string :as string]))


(defn linklist[]
  (map-indexed (fn [index name]
         [:li [:a {:href (str/lower-case name)} (str index "." name)]])
       ["SCENETE AUDIO"
        "EMISIUNI TV"
        "ANIMATII DIVERSE"
        "MUZICA"
        "DESPRE"]))

(defn page []
  (crt-page
    [:div.plamain
     [:h1 "planeta moldova"]
     [:ul (linklist)]
     [:input {:type "text"}]]))
