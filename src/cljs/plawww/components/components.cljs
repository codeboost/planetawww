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
                   which the parent can call to jump to the full text.
  While typing, the typing position is kept in view when the text is taller than
  its scroll container (e.g. on phones). A skip leaves the scroll position alone."
  [{:keys [text speed-ms on-complete skip-atom] :or {speed-ms 25}}]
  (let [pos (r/atom 0)
        interval-id (atom nil)
        completed? (atom false)
        skipped? (atom false)
        cursor-el (atom nil)
        cursor-ref #(reset! cursor-el %)
        complete! (fn []
                    (js/clearInterval @interval-id)
                    (when-not @completed?
                      (reset! completed? true)
                      (when on-complete (on-complete))))
        skip! (fn []
                (reset! skipped? true)
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

      :component-did-update
      (fn []
        ;Follow the typing: scrolls only when the current line is out of view.
        ;Not after completion, so a late render can't cut short a scroll
        ;started by the parent's on-complete.
        (when-let [el (and (not @skipped?) (not @completed?) @cursor-el)]
          (.scrollIntoView el #js {:block "nearest"})))

      :component-will-unmount
      (fn []
        (js/clearInterval @interval-id)
        (when skip-atom (reset! skip-atom nil)))

      :reagent-render
      (fn []
        ;The not-yet-typed remainder is rendered invisibly, so the text is
        ;laid out at its final size and line breaks from the first frame;
        ;nothing reflows (or scrolls) while typing.
        [:span.typewriter
         (subs text 0 @pos)
         ;Empty marker at the typing position. scrollIntoView treats it as a
         ;zero-height point at the top of its line; the scroll margin makes the
         ;whole line (plus some breathing room) count as the target.
         [:span.typewriter-cursor {:ref cursor-ref
                                   :style {:scroll-margin-bottom "2em"}}]
         [:span.typewriter-rest {:style {:visibility "hidden"}}
          (subs text @pos)]])})))

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
