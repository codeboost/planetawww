(ns ^:figwheel-no-load plawww.dev
  (:require [plawww.core :as core]
            [figwheel.client :as figwheel :include-macros true]
            [devtools.core :as devtools]))

(enable-console-print!)

(devtools/install!)

(figwheel/watch-and-reload
  ;:websocket-url "ws://192.168.1.38:3449/figwheel-ws"
  :jsload-callback core/mount-root)

(core/init!)
