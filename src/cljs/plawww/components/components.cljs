;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.components.components
  (:require
   [reagent.core :as r]
   [plawww.paths :as paths]))

(defn typewriter
  "Renders `text` with a typing effect, one character per `speed-ms` (default 25).
  Options:
    :text        - the full text to type out.
    :speed-ms    - delay between characters, in milliseconds.
    :on-complete - called once, when the full text is displayed (also after a skip).
    :skip-atom   - optional atom; the component stores a 0-arg `skip!` fn in it,
                   which the parent can call to jump to the full text."
  [{:keys [text speed-ms on-complete skip-atom] :or {speed-ms 25}}]
  (let [pos (r/atom 0)
        interval-id (atom nil)
        completed? (atom false)
        complete! (fn []
                    (js/clearInterval @interval-id)
                    (when-not @completed?
                      (reset! completed? true)
                      (when on-complete (on-complete))))
        skip! (fn []
                (reset! pos (count text))
                (complete!))]
    (when skip-atom
      (reset! skip-atom skip!))
    (r/create-class
     {:display-name "typewriter"

      :component-did-mount
      (fn []
        (reset! interval-id
                (js/setInterval
                 (fn []
                   (if (< @pos (count text))
                     (swap! pos inc)
                     (complete!)))
                 speed-ms)))

      :component-will-unmount
      (fn []
        (js/clearInterval @interval-id)
        (when skip-atom (reset! skip-atom nil)))

      :reagent-render
      (fn []
        [:span.typewriter (subs text 0 @pos)])})))

(defn minimise-button
  [text on-click]
  [:div.min-button
   {:on-click on-click
    :style {:cursor :pointer}}
   text])


(defn tag-list-component [tags on-click]
  [:div.tags-container
   (into
    [:ul.tags]
    (for [tag tags]
      ^{:key tag}
      [:li
       [:a
        {:href     (paths/tags-path tag)
         :on-click on-click}
        tag]
       " "]))])
