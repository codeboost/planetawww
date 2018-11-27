;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.ui
  (:require
   [cljsjs.react.dom]
   [reagent.core :as r]))

(defn mount-to-body
  "The mount-to-body component takes care of mounting a given children to a div
   in body. In fact, it uses the document.body.appendChild function and uses
   unstable_renderSubtreeIntoContainer to preserve React context.

  Properties:
    - did-mount   fn(component: HTMLElement)  Callback when the component has
                                              been mounted to body.
    - did-update  fn(component: HTMLElement)  Callback when the component has
                                              been updated.

  Children:
    - It takes exactly one child (a component) and will render it."
  [{:keys [did-mount]} _]
  (let [target  (atom nil)
        render  (fn [this & [callback]]
                  (.unstable_renderSubtreeIntoContainer
                   js/ReactDOM
                   this                 ; conceptual parent of this render tree
                   (r/as-element (first (r/children this)))
                   @target
                   callback))]
    (r/create-class
     {:display-name
      "mount-to-body"

      :component-did-mount
      (fn [this]
        (let [target-element (.createElement js/document "div")]
          (set! (.-className target-element) "pm-mount-to-body")
          (.appendChild (.-body js/document) target-element)
          (reset! target target-element)
          (render this
                  (when did-mount #(did-mount (.-firstElementChild @target))))))

      :component-did-update
      (fn [this _]
        (let [did-update (:did-update (r/props this))]
          (render this (when did-update #(did-update (.-firstElementChild @target))))))

      :component-will-unmount
      (fn [_]
        (.unmountComponentAtNode js/ReactDOM @target)
        (.removeChild (.-body js/document) @target))

      :reagent-render
      (fn [_ _] nil)})))

(defn overlay
  "Overlay is a generic low-level component for rendering content above its
   siblings, or above the entire application. It uses the mount-to-body
   component to make sure it will properly render.

  Properties:
   - It accepts the standard properties for an HTML div element.

  Child:
   - It accepts one and only one child."
  [props child]
  [mount-to-body {}
   [:div.pm-overlay props
    child]])

(defn modal
  "A modal presents content overlaid over other parts of the UI.
  It uses the overlay component to achieve this goal.

  Properties:
    - on-close    fn(event: ClickEvent)   - This callback is triggered when the modal wants to close.
    - visible?    boolean                 - Visible? to true will mount the modal into the body. Default: false.

  Children:
    - It accepts one and only one children and it's the content of the modal."
  [{:keys [on-close]} _]
  (let [node               (atom nil)
        key-handler        (fn [e]
                             (when (= (.-keyCode e) 27)     ;; Escape key
                               (on-close e)))
        mouse-down-handler (fn [e]
                             ;; Close the modal only if the overlay div is clicked
                             (when (= (.-target e) (.-currentTarget e))
                               (let [{:keys [on-close]} (r/props @node)]
                                 (on-close e))))]

    (r/create-class
     {:display-name
      "modal"

      :component-did-mount
      (fn [this]
        (reset! node this)
        (.addEventListener js/window "keydown" key-handler))

      :component-did-update
      (fn [this [_]]
        (let [{:keys [visible?]} (r/props this)]
          (.removeEventListener js/window "keydown" key-handler)
          (if visible?
            (.addEventListener js/window "keydown" key-handler)
            (.removeEventListener js/window "keydown" key-handler))))

      :component-will-unmount
      (fn [_]
        (.removeEventListener js/window "keydown" key-handler)
        (reset! node nil))

      :reagent-render
      (fn [{:keys [on-close visible? class] :or {visible? false} :as props} content]
        (let [class (remove nil? (if (seq? class) class [class]))])
        (when visible?
          [overlay {:class (into [:pm-modal--overlay :show-scaled] class)
                    :on-mouse-down mouse-down-handler
                    :style {:animation-duration "0.2s"}}
           [:div.pm-modal--container
            [:div.pm-modal (dissoc props :on-close :visible?)
             #_[:div.pm-modal--close {:on-click on-close} "x"]
             content]]]))})))


(defn share-dialog-modal [_]
  (let [state (r/atom {:copied? false
                       :input-element nil})]
    (fn [{:keys [on-close share-url]}]
      (let [{:keys [copied? input-element]} @state]
        [modal
         {:visible? true
          :class [:share-dialog-modal]
          :on-close on-close}
         [:div.share-dialog-content
          [:div.min-button [:a {:href :#
                                :on-click on-close} "x"]]
          [:div.dialog-container
           [:div.dialog-content
            [:div.title [:strong "SHAREUIESTE URL-ul"]]

            (if copied?
              [:h1 "COPIAT!"]
              [:div.controls
               [:input {:type :text
                        :value share-url
                        :cols 50
                        :read-only true
                        :selected true
                        :ref #(swap! state assoc :input-element %)}]
               [:a.toggle-button.copy-button {:href :#
                                              :on-click (fn []
                                                          (when input-element
                                                            (.select input-element)
                                                            (.execCommand js/document "copy")
                                                            (swap! state assoc :copied? true))
                                                          (js/setTimeout #(on-close) 1000))}
                "COPIAZA"]])]]]]))))