(ns plawww.paths)

(defn s-image-path [id]
  (str "/data/images/media/" id "s.jpg"))

(defn l-image-path [id]
  (str "/data/images/media/" id "l.jpg"))

(defn media-path [filename]
  (str "/data/media/" filename))