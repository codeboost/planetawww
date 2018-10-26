;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.paths)

(def AUDIO_IMAGE_PATH "/images/planeta.png")

(defn s-image-path [id]
  AUDIO_IMAGE_PATH
  #_(str "/data/images/media/" id "s.jpg"))

(defn l-image-path [id]
  AUDIO_IMAGE_PATH
  #_(str "/data/images/media/" id "l.jpg"))

(defn media-path [filename]
  (str "/data/media/" filename))

(def storage-file 1)
(def storage-youtube 3)

(defn item-path [{:keys [unique_id storage_id filename]}]
  (if (= storage_id storage-youtube)
    (str "https://www.youtube.com/watch?v=" unique_id)
    (media-path filename)))