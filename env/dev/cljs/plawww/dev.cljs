(ns ^:figwheel-no-load plawww.dev
  (:require [plawww.core :as core]
            [figwheel.client :as figwheel :include-macros true]))

(enable-console-print!)

(figwheel/watch-and-reload
  :xwebsocket-url "ws://192.168.1.38:3449/figwheel-ws"
  :websocket-url "ws://127.0.0.1:3449/figwheel-ws"
  :jsload-callback core/mount-root)

(core/init!)
