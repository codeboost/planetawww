(defproject plawww "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[binaryage/devtools "1.0.0"]
                 [binaryage/dirac "1.5.11"]
                 [com.cemerick/url "0.1.1"]
                 [clj-http "3.10.1"]
                 [cljsjs/typedjs "1.1.1-0"] ;jquery plugin that types
                 [cljsjs/react-player "1.11.0-0"]
                 [compojure "1.6.1"]
                 [cljsjs/google-analytics "2017.09.21-0"]
                 [hiccup "1.0.5"]
                 [honeysql "0.9.10"]
                 [mysql/mysql-connector-java "8.0.20"]
                 [org.clojure/java.jdbc "0.7.11"]
                 [org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.339" :scope "provided"]
                 [org.clojure/core.async "1.1.587"]
                 [reagent "0.8.1"]
                 [reagent-utils "0.3.1"]
                 [ring "1.8.0"]
                 [ring-server "0.5.0"]
                 [ring/ring-defaults "0.3.2"]
                 [secretary "1.2.3"]
                 [venantius/accountant "0.2.4" :exclusions [org.clojure/tools.reader]]
                 [yogthos/config "1.1.7"]]


  :plugins [[lein-environ "1.0.2"]
            [lein-cljsbuild "1.1.7"]
            [lein-asset-minifier "0.2.7"
             :exclusions [org.clojure/clojure]]]

  :ring {:handler plawww.handler/app
         :uberwar-name "plawww.war"}

  :min-lein-version "2.5.0"

  :uberjar-name "planeta-crt.jar"

  :main plawww.server

  :clean-targets ^{:protect false}
  [:target-path
   [:cljsbuild :builds :app :compiler :output-dir]
   [:cljsbuild :builds :app :compiler :output-to]]

  :source-paths ["src/clj" "src/cljc"]
  :resource-paths ["resources" "target/cljsbuild" ".semver"]

  :minify-assets  {:assets
                   {"resources/public/css/crt/crt.min.css" "resources/public/css/crt/crt.css"}}


  :cljsbuild
  {:builds {
            :min
            {:source-paths ["src/cljs" "src/cljc" "env/prod/cljs"]
             :compiler
             {:output-to "target/cljsbuild/public/js/app.js"
              :output-dir "target/uberjar"
              :optimizations :advanced
              :foreign-libs [{:file "resources/public/lib/screenfull.js"
                              :provides ["screenfull"]}]
              ;Created by the generate-extern tool
              :externs ["externs.js" "react-player-externs.js"]
              :optimize-constants true
              :pseudo-names false
              :pretty-print false}}
            :app
            {:source-paths ["src/cljs" "src/cljc" "env/dev/cljs"]
             :compiler
             {:main "plawww.dev"
              :asset-path "/js/out"
              :output-to "target/cljsbuild/public/js/app.js"
              :output-dir "target/cljsbuild/public/js/out"
              :source-map true
              :optimizations :none
              :pretty-print  true
              :foreign-libs [{:file "resources/public/lib/screenfull.js"
                              :provides ["screenfull"]}]}}


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

                   :dependencies [[ring/ring-mock "0.4.0"]
                                  [ring/ring-devel "1.8.0"]
                                  [prone "2020-01-17"]
                                  [figwheel-sidecar "0.5.16"]
                                  [org.clojure/tools.nrepl "0.2.13"]
                                  [com.cemerick/piggieback "0.2.2"]
                                  [devcards "0.2.6"]
                                  [pjstadig/humane-test-output "0.10.0"]]


                   :source-paths ["env/dev/clj"]
                   :plugins [[lein-figwheel "0.5.16"]
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
