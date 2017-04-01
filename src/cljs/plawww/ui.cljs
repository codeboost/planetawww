(ns plawww.ui)


(defn list-view-cell[image content accessory-view]
  [:div.lv-cell.hstack
   [:div.image-area
    [:img.image {:src image}]]
   [:div.content-area content]
   [:div.accessory-area accessory-view]])