;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.paths)

(def AUDIO_IMAGE_PATH "/images/planeta.png")
(def EXPLORER_PATH "/pragoane")

(defn s-image-path [id]
  AUDIO_IMAGE_PATH
  #_(str "/data/images/media/" id "s.jpg"))

(defn l-image-path [id]
  AUDIO_IMAGE_PATH
  #_(str "/data/images/media/" id "l.jpg"))

(defn media-path [filename]
  (str "/data/media/" filename))

(defn explorer-path
  "Returns the relative path to the media explorer.
  subpath must be a relative path (eg. not start with /).
  If subpath is empty, the root explorer path is returned."
  [subpath]
  (let [subpath (str subpath)]
    (if-not (empty? subpath)
      (str EXPLORER_PATH "/" subpath)
      EXPLORER_PATH)))

(def storage-file 1)
(def storage-youtube 3)

(defn item-path [{:keys [unique_id storage_id filename]}]
  (if (= storage_id storage-youtube)
    (str "https://www.youtube.com/watch?v=" unique_id)
    (media-path filename)))