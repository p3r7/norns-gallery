(defproject norns-index "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "MIT"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.758"
                  :exclusions [com.google.javascript/closure-compiler-unshaded
                               org.clojure/google-closure-library
                               org.clojure/google-closure-library-third-party]]
                 [com.taoensso/encore "3.18.0"]
                 [org.clojure/tools.cli "1.0.214"]
                 [org.clojure/core.async "0.4.500"]
                 ;; [com.rpl/specter "1.1.3"]
                 ;; [yogthos/config "1.1.5"]

                 ;; observability
                 ;; [com.taoensso/timbre "4.10.0"
                 ;; :exclusions [com.taoensso/encore]]

                 ;; client-side code (SPA)
                 [thheller/shadow-cljs "2.11.18"]
                 [hiccup "1.0.5"]
                 [reagent "1.2.0"]
                 ;; [re-frame "0.12.0"]
                 ;; [day8.re-frame/http-fx "0.2.1"]
                 ;; [re-com "2.9.0"]
                 [cljs-http "0.1.46"]
                 [com.cemerick/url "0.1.1"]]

  :plugins   [
              [refactor-nrepl "3.6.0"]
              [cider/cider-nrepl "0.30.0"]
              [lein-cljsbuild "1.1.8"]
              ]

  :source-paths ["src/cljc"
                 "src/cljs"
                 ;; "src/js"
                 ]

  :target-path   "target/%s"
  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  ;; for shadow-cljs
  :aliases {"npm-deps"
            ["run" "-m" "shadow.cljs.devtools.cli" "--npm" "npm-deps"]
            ;; "static-build"
            ;; NB: shadow-cljs run only runs clj, not cljs!
            ;; ["with-profile" "static-build" "run" "-m" "shadow.cljs.devtools.cli" "run" "prerender/-main"]
            "dev"
            ["with-profile" "dev" "run" "-m" "shadow.cljs.devtools.cli" "watch" "app"]
            "prod"
            ["with-profile" "prod" "run" "-m" "shadow.cljs.devtools.cli" "release" "app"]
            }

  :profiles
  {:dev  {:dependencies [[binaryage/devtools "1.0.2"]
                         ;; [day8.re-frame/re-frame-10x "0.6.5"]
                         ;; [day8.re-frame/tracing "0.5.5"]
                         ]}

   :prod {:dependencies [
                         ;; [day8.re-frame/tracing-stubs "0.5.5"]
                         ]}

   :static-build {
                  :source-paths ["src/cljc" "src/cljs" "prerender/cljs"]
                  }
   }

  :cljsbuild
  {:builds
   [{:id "prerender"
     :source-paths ["src/cljc" "src/cljs" "prerender/cljs"]
     :compiler {:main "norns-index.prerender"
                :target :nodejs
                :output-dir "target/cljsbuild/prerender/out"
                :output-to "target/cljsbuild/prerender/main.js"
                :npm-deps true
                :aot-cache true}}
    {:id "prerender-state"
     :source-paths ["src/cljc" "src/cljs" "prerender/cljs"]
     :compiler {:main "norns-index.prerender-state"
                :target :nodejs
                :output-dir "target/cljsbuild/prerender-state/out"
                :output-to "target/cljsbuild/prerender-state/main.js"
                :npm-deps true
                :aot-cache true}}]
   }
  )
