(defproject plawww "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.async "0.4.474"]
                 [org.clojure/spec.alpha "0.1.143"]
                 [org.clojure/test.check "0.9.0"]
                 [org.clojure/clojurescript "1.10.238" :scope "provided"]
                 [environ "1.1.0"]
                 [binaryage/devtools "0.9.10"]
                 [clj-http "3.8.0"]
                 [cljsjs/typedjs "1.1.1-0"] ;jquery plugin that types
                 [cljsjs/react-draggable "3.0.3-0"] ;draggable
                 [compojure "1.6.0"]
                 [garden "1.3.5"]
                 [hiccup "1.0.5"]
                 [hikari-cp "2.3.0"]
                 [camel-snake-kebab "0.4.0"]
                 [reagent "0.7.0"]
                 [reagent-utils "0.3.1"]
                 [ring "1.6.3"]
                 [ring-server "0.5.0"]
                 [ring/ring-defaults "0.3.1"]
                 [secretary "1.2.3"]
                 [venantius/accountant "0.2.4" :exclusions [org.clojure/tools.reader]]
                 [yogthos/config "1.1.1"]
                 [org.postgresql/postgresql "42.2.2"]
                 [honeysql "0.9.2"]
                 [org.clojure/java.jdbc "0.7.5"]]


  :plugins [[lein-environ "1.0.2"]
            [lein-cljsbuild "1.1.1"]
            [lein-asset-minifier "0.2.7"
             :exclusions [org.clojure/clojure]]]

  :ring {:handler plawww.handler/app
         :uberwar-name "plawww.war"}

  :min-lein-version "2.5.0"

  :uberjar-name "plawww.jar"

  :main plawww.server

  :clean-targets ^{:protect false}
  [:target-path
   [:cljsbuild :builds :app :compiler :output-dir]
   [:cljsbuild :builds :app :compiler :output-to]]

  :source-paths ["src/clj" "src/cljc"]
  :resource-paths ["resources" "target/cljsbuild"]

  :minify-assets
  {:assets
   {"resources/public/css/site.min.css" "resources/public/css/site.css"}}

  :cljsbuild
  {:builds {
            :min
            {:source-paths ["src/cljs" "src/cljc" "env/prod/cljs"]
             :compiler
             {:output-to "target/cljsbuild/public/js/app.js"
              :output-dir "target/uberjar"
              :optimizations :advanced
              :pretty-print  false}}
            :app
            {:source-paths ["src/cljs" "src/cljc" "env/dev/cljs"]
             :compiler
             {:main "plawww.dev"
              :asset-path "/js/out"
              :output-to "target/cljsbuild/public/js/app.js"
              :output-dir "target/cljsbuild/public/js/out"
              :source-map true
              :optimizations :none
              :pretty-print  true}}


            :devcards
            {:source-paths ["src/cljs" "src/cljc" "env/dev/cljs"]
             :figwheel {:devcards true}
             :compiler {:main "plawww.cards"
                        :asset-path "js/devcards_out"
                        :output-to "target/cljsbuild/public/js/app_devcards.js"
                        :output-dir "target/cljsbuild/public/js/devcards_out"
                        :source-map-timestamp true
                        :optimizations :none
                        :pretty-print true}}}}




  :figwheel
  {:http-server-root "public"
   :server-port 3449
   :nrepl-port 7002
   :nrepl-middleware ["cemerick.piggieback/wrap-cljs-repl"]

   :css-dirs ["resources/public/css"]
   :ring-handler plawww.handler/app}

  :less {:source-paths ["src/less"]
         :target-path "resources/public/css"}


  :profiles {:dev {:repl-options {:init-ns plawww.repl
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

                   :dependencies [[ring/ring-mock "0.3.2"]
                                  [ring/ring-devel "1.6.3"]
                                  [prone "1.5.1"]
                                  [figwheel-sidecar "0.5.15"]
                                  [org.clojure/tools.nrepl "0.2.13"]
                                  [com.cemerick/piggieback "0.2.2"]
                                  [devcards "0.2.4"]
                                  [pjstadig/humane-test-output "0.8.3"]]


                   :source-paths ["env/dev/clj"]
                   :plugins [[lein-figwheel "0.5.15"]
                             [lein-less "1.7.5"]]

                   :injections [(require 'pjstadig.humane-test-output)
                                (pjstadig.humane-test-output/activate!)]

                   :env {:dev true}}

             :uberjar {:hooks [minify-assets.plugin/hooks]
                       :source-paths ["env/prod/clj"]
                       :prep-tasks ["compile" ["cljsbuild" "once" "min"]]
                       :env {:production true}
                       :aot :all
                       :omit-source true}})
