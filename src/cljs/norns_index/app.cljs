(ns norns-index.app
  (:require
   ;; core
   [clojure.edn :as edn]
   [goog.dom :as gdom]
   [norns-index.utils.core :refer [member? remove-nils]]

   ;; react
   [reagent.core :as r]
   [reagent.dom :as rdom]

   ;; app
   [norns-index.views :as views]
   ))


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
  "Mount transpilled js code into #app dome element.
  Gets called both at page load (`init`) and on automatic code reload by shadow-cljs (`on-reload`)."
  []
  (mount-app-element [views/main-view]))



;; HELPERS - DOM

(defn mount
  "Mount and render hiccup COMPONENT on dom element EL."
  [component el]
  (rdom/render component el))

(defn mount-app-element
  "Mount hiccup COMPONENT on dom element #app."
  [component]
  (when-let [el (app-element)]
    (mount component el)))

(defn app-element []
  ;; NB: we often also see:
  ;; (.getElementById js/document "app")
  (gdom/getElement "app"))
