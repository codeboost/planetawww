(ns plawww.paths)

(defn s-image-path [id]
  (str "/data/images/media/" id "s.jpg"))

(defn l-image-path [id]
  (str "/data/images/media/" id "l.jpg"))

(defn media-path [filename]
  (str "/data/media/" filename))

(def storage-file 1)
(def storage-youtube 3)

(defn item-path [{:keys [unique_id storage_id filename]}]
  (if (= storage_id storage-youtube)
    (str "https://www.youtube.com/watch?v=" unique_id)
    (media-path filename)))