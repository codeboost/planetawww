;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.media-item.media-item
  (:require [reagent.core :as r]
            [plawww.components.components :refer [tag-list-component]]
            [plawww.mediadb.core :as db]
            [plawww.paths :as paths]
            [plawww.ui :as ui]
            [plawww.utils :as utils]))

(defn artwork-bg-image [url]
  (str "url(" url ")"))

(defn toolbar-item [title on-click]
  [:div.toolbar-item
   {:on-click on-click}
   title])

(defn action-buttons [state {:keys [selected-item playing-item playing? on-play]}]
  (let [play-button-text (if (and playing? (= (:id selected-item) (:id playing-item)))
                           "PAUZA" "PLAY")]
    [:div.toolbar
     [toolbar-item play-button-text on-play]

     [toolbar-item "SHARE" #(swap! state assoc :share-dialog-visible? true)]
     (when (= "audio" (:type selected-item))
       [:div.toolbar-item
        [:a {:href (paths/item-path selected-item)
             :target :download-the-pragon}
         "DOWNLOAD"]])]))


(defn img-container [_]
  (let [dom-el (r/atom nil)
        transform-style (r/atom nil)
        display (r/atom :none)
        resize-proportionally
        (fn []
          (let [el @dom-el
                w (.-offsetWidth el)
                h (.-offsetHeight el)]
            (if (and (pos? w) (pos? h))
              (let [scale-x (when (> w h) (/ h w))
                    scale-y (when (> h w) (/ w h))
                    scale-x (when scale-x (str "scaleX(" scale-x ")"))
                    scale-y (when scale-y (str "scaleY(" scale-y ")"))]
                (reset! transform-style (or scale-x scale-y)))
              (reset! transform-style nil))))
        on-image-load
        (fn []
          (reset! display :block)
          (resize-proportionally))]


      (r/create-class
       {:component-did-mount
        (fn []
          (.addEventListener js/window "resize" resize-proportionally)
          (resize-proportionally))

        :component-did-update
        (fn [_]
          (resize-proportionally))

        :component-will-unmount
        (fn []
          (.removeEventListener js/window "resize" resize-proportionally))
        :reagent-render
        (fn [{:keys [title tags id description_plain type] :as item}]
          (let [display (and @transform-style @display)]
            [:div.img-container {:ref #(reset! dom-el %)}
             [:img {:src (paths/media-image-path id {:show-custom? (= type "video")
                                                     :category-name (db/any-category-slug item)
                                                     :size :large})
                    :style {:transform @transform-style
                            :display (or display :none)}
                    :on-load on-image-load}]]))})))


(defn info-component [{:keys [selected-item action-buttons] :as opts}]
  (let [{:keys [title tags duration id description_plain type publish_on]} selected-item]
    [:div.media-item-info
     [:div.title title]
     [tag-list-component tags #()]
     [:div.type [:span.key "Tip:" [:span.value type]]]
     [:div.duration [:span.key "Durata:"] [:span.value (utils/format-duration duration "%sm %ss")]]
     [:div.det.publish-on [:span.key "Publicat:" [:span.value (utils/format-date publish_on)]]]
     [:div.album-art-container [img-container selected-item]]
     [:div.description description_plain]
     action-buttons]))

(defn item-info-component
  "Item info component.
  `opts` should contain the following keys:
    :on-close  - handler for when the close button is clicked
    :on-play   - handler for when the play button is clicked
    :selected-item  - the media item to display
    :playing-item  - item currently loaded in the player
    :playing? - boolean true when the player is playing and not paused.

    `:playing-item` and `:playing?` are used to determine the state of the 'play' button.
    "
  [_]
  (let [state (r/atom {:section :info
                       :share-dialog-visible? false})]
    (fn [{:keys [on-close selected-item] :as opts}]
      [:div.media-item-info-container
       (when on-close
         [:div.min-button [:a {:on-click on-close :href :#} "x"]])
       (case (:section @state)
         :info
         [info-component (assoc opts :action-buttons [action-buttons state opts])])


       (when (:share-dialog-visible? @state)
         [ui/share-dialog-modal {:on-close #(swap! state assoc :share-dialog-visible? false)
                                 :share-url (.-href (.-location js/window))}])])))



