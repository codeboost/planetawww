;   Copyright (c) Braghis Florin. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php).
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns plawww.media-page
  (:require [clojure.string :as str]
            [clojure.core.async :refer (chan put! <!)]
            [cljsjs.typedjs]
            [clojure.string :as str]
            [reagent.core :as r]
            [plawww.media-item-detail :as media-item-detail]
            [plawww.search-component :refer [search-component]]
            [plawww.text-menu-component :refer [menu->hiccup]]))
