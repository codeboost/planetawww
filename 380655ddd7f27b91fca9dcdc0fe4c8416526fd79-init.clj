nil (do (set! *warn-on-reflection* nil) (require (quote figwheel-sidecar.repl-api)) (try (do (clojure.core/let [figwheel-sidecar-version__661__auto__ (clojure.core/when-let [version-var__662__auto__ (clojure.core/resolve (quote figwheel-sidecar.config/_figwheel-version_))] (clojure.core/deref version-var__662__auto__))] (if (clojure.core/not= "0.5.15" figwheel-sidecar-version__661__auto__) (clojure.core/println (clojure.core/str "Figwheel version mismatch!!\n" "You are using the lein-figwheel plugin with version: " (clojure.core/pr-str "0.5.15") "\n" "With a figwheel-sidecar library with version:        " (clojure.core/pr-str figwheel-sidecar-version__661__auto__) "\n" "\n" "These versions need to be the same.\n" "\n" "Please look at your project.clj :dependencies to see what is causing this.\n" "You may need to run \"lein clean\" \n" "Running \"lein deps :tree\" can help you see your dependency tree.")) (do (figwheel-sidecar.repl-api/system-asserts) (figwheel-sidecar.repl-api/launch-from-lein (quote {:data {:cljsbuild {:builds {:min {:source-paths ["src/cljs" "src/cljc" "env/prod/cljs"], :compiler {:output-to "target/cljsbuild/public/js/app.js", :output-dir "target/uberjar", :optimizations :advanced, :pretty-print false}}, :app {:source-paths ["src/cljs" "src/cljc" "env/dev/cljs"], :compiler {:main "plawww.dev", :asset-path "/js/out", :output-to "target/cljsbuild/public/js/app.js", :output-dir "target/cljsbuild/public/js/out", :source-map true, :optimizations :none, :pretty-print true}}, :devcards {:source-paths ["src/cljs" "src/cljc" "env/dev/cljs"], :figwheel {:devcards true}, :compiler {:main "plawww.cards", :asset-path "js/devcards_out", :output-to "target/cljsbuild/public/js/app_devcards.js", :output-dir "target/cljsbuild/public/js/devcards_out", :source-map-timestamp true, :optimizations :none, :pretty-print true}}}}, :figwheel {:http-server-root "public", :server-port 3449, :nrepl-port 7002, :nrepl-middleware ["cemerick.piggieback/wrap-cljs-repl"], :css-dirs ["resources/public/css"], :ring-handler plawww.handler/app, :open-file-command "figwheel-file-opener", :server-logfile "log/figwheel.log"}}, :file "project.clj", :profile-merging true, :simple-merge-works true, :active-profiles (:base :system :user :provided :dev)}) (quote ["app"]))))) (java.lang.System/exit 0)) (catch java.lang.Exception e__651__auto__ (do (.printStackTrace e__651__auto__) (java.lang.System/exit 1)))))