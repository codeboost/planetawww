(ns plawww.utils
  (:require [goog.string :as gstring]
            [goog.string.format]))


(defn format-duration [timestamp]
  (let [minutes (quot timestamp 60)
        seconds (mod timestamp 60)]
    (gstring/format "%02d:%02d" minutes seconds)))