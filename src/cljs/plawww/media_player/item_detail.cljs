;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.media-player.item-detail
  (:require [reagent.core :as r]
            [plawww.paths :as paths]
            [plawww.utils :as utils]
            [cljs.test :refer-macros [deftest is testing run-tests]]
            [cljs.core.async :refer [put!]])
  (:import [goog.async Debouncer]))

(defn debounce [f interval]
  (let [dbnc (Debouncer. f interval)]
    ;; We use apply here to support functions of various arities
    (fn [& args] (.apply (.-fire dbnc) dbnc (to-array args)))))


(defn image-path [item]
  (let [id (:id item)]
    (paths/s-image-path id)))

(defn detail-image-path [item]
  (let [id (:id item)]
    (paths/l-image-path id)))

(defn item-href [id]
  (str "/media/" id))

(defn duration-comp-svg [{:keys [duration item played]}]
  (let [duration (if (zero? duration) (or (:duration item) 0) duration)]
    [:svg {:viewBox "0 0 50 20"}
     [:text {:x 0 :y "15"}
      (str (utils/format-duration (* played duration)) "/" (utils/format-duration duration))]]))

(defn duration-comp
  [_]
  (let [duration-hide-timer (r/atom 0)
        flash-duration (fn []
                         (js/clearTimeout @duration-hide-timer)
                         (reset!
                          duration-hide-timer
                          (js/setTimeout #(reset! duration-hide-timer 0) 1000)))]
    (fn [{:keys [duration item played playing]}]
      (let [duration (if (zero? duration) (or (:duration item) 0) duration)
            display-type (if (and (= 0 @duration-hide-timer) playing) :played :duration)
            text (if (= display-type :played)
                   [:span.played (utils/format-duration (* played duration))]
                   [:span.duration (utils/format-duration duration)])]
        @duration-hide-timer
        [:div.position-and-duration
         {:on-click #(flash-duration)}
         text]))))
