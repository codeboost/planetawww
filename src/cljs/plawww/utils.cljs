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

(defn starts-with-letter?
  "Returns true if `word` starts with the letter.
   If `letter` is '#', will check if `word` starts with a digit (0-9).
   `letter` can be any substring of length >= 1."
  [word letter]
  (cond
    (zero? (count letter)) false
    (= "#" letter) (some? (re-matches #"[0-9]+" (first (str/trim word))))
    :else (str/starts-with? (str/lower-case word) (str/lower-case letter))))

(defn items-starting-with-letter
  "Filters `coll`, keeping items which start with the substring in `letter`.
  Uses `starts-with-letter?`, so the same matching semantics apply to every item in `coll`.
  Note that `letter` can be any substring of one or more characters.
  Returns:
    Potentially empty collection of strings from `coll`, which start with `letter`."
  [coll letter]
  (filter #(starts-with-letter? % letter)) coll)

(defn first-letters [items]
   (into (sorted-set) (map #(extract-first-letter (:title %)) items)))



(comment
  ;TODO: Move to unit test

  (deftest test-starts-with-first-letter?
           (is (= true (starts-with-letter? "a" "a")))
           (is (= true (starts-with-letter? "Alpha" "A")))
           (is (= true (starts-with-letter? "beta" "B")))
           (is (= false (starts-with-letter? "34" "B")))
           (is (= true (starts-with-letter? "34" "#"))))

  (deftest test-extract-first-letter
           (is (= "A" (extract-first-letter "Alpha")))
           (is (= "Z" (extract-first-letter "zebra")))
           (is (= "#" (extract-first-letter "10 Lions")))
           (is (= "#" (extract-first-letter "1984")))
           (is (= "#" (extract-first-letter ""))))

  (deftest test-first-letters
           (is
            (= #{"#" "A" "B" "G"}
               (first-letters [{:title "Alpha"}
                               {:title "Beta"}
                               {:title "Gamma"}
                               {:title "54"}])))))
