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
   ["iframe-resizer" :as iframe-resizer]
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

  (let [query-params (:query (url/url (-> js/window .-location .-href)))
        random (get query-params "random")
        category (get query-params "category")
        connectivity (get query-params "connectivity")
        author (get query-params "author")]
    (cond
      random
      (mount-app-element [views/main-view-random (edn/read-string random)])

      category
      (mount-app-element [views/main-view-single-category category])

      connectivity
      (mount-app-element [views/main-view-single-connectivity-feature connectivity])

      author
      (mount-app-element [views/main-view-single-author author])

      :else
      (mount-app-element [views/main-view-all]))

    (dynamic-conf/get-from-wiki-js)) ; NB: gets stored in `norns-index.state/state`
  )



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
