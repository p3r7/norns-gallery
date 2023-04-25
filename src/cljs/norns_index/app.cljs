(ns norns-index.app
  (:require
   ;; core
   [clojure.edn :as edn]
   [goog.dom :as gdom]
   [norns-index.utils.core :refer [member? remove-nils]]

   ;; window/dom
   [cemerick.url :as url]

   ;; react
   [reagent.core :as r]
   [reagent.dom :as rdom]

   ;; state
   [norns-index.dynamic-conf :as dynamic-conf]

   ;; app
   [norns-index.views :as views]

   ;; js libs
   ;; [iframe-resizer :as iframe-resizer]
   [react-dom :as react-dom]))


(declare mount-root mount-app-element app-element
         ;; views
         main-view filter-panel
         script-panel screenshot feature
         script-category-section)



;;  ENTRY POINT

(defn init
  "Main entry point.
  Called (only) on first page load."
  []
  (mount-root))

(defn ^:dev/after-load on-reload
  "Called (only) when code gets reloaded by shadow-cljs."
  []
  (mount-root)
  (js/console.debug "code reloaded by shadow-cljs"))

(defn mount-root
  "Mount transpilled js code into #app DOM element.
  Gets called both at page load (`init`) and on automatic code reload by shadow-cljs (`on-reload`)."
  []

  (mount-app-element [views/main-view-all])
  (dynamic-conf/get-script-index!))



;; HELPERS - DOM

(defonce is-first-load (atom true))

(defn mount
  "Mount and render hiccup COMPONENT on dom element EL."
  [component el]

  (let [at-first-load @is-first-load]
    (if (and at-first-load (.hasChildNodes el))
      (react-dom/hydrate (r/as-element component) el)
      (rdom/render component el))

    (when at-first-load
      (reset! is-first-load false))))

(defn mount-app-element
  "Mount hiccup COMPONENT on dom element #app."
  [component]
  (when-let [el (app-element)]
    (mount component el)))

(defn app-element []
  ;; NB: we often also see:
  ;; (.getElementById js/document "app")
  (gdom/getElement "app"))
