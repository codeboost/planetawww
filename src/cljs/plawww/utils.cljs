(ns plawww.utils
  (:require
   [clojure.string :as str]
   [goog.string :as gstring]
   [goog.string.format]))



(defn format-duration [timestamp]
  (let [minutes (quot timestamp 60)
        seconds (mod timestamp 60)]
    (gstring/format "%02d:%02d" minutes seconds)))

(defn extract-first-letter [str]
  (let [letter (or (first (str/trim str)) "#")
        letter (str/upper-case letter)
        letter (if (re-matches #"[0-9]+" letter)
                 "#"
                 letter)]
    letter))

(defn starts-with-letter? [word letter]
  (cond
    (= "#" letter) (some? (re-matches #"[0-9]+" (first (str/trim word))))
    :else (str/starts-with? (str/lower-case word) (str/lower-case letter))))
