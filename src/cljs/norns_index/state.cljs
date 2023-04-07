(ns norns-index.state
  (:require
   [reagent.core :as r]

   [norns-index.conf :as conf]
   [norns-index.init-state :as init-state]))



;; STATE

(defonce state (r/atom init-state/state))



;; FILTER

(defn show-script? [script-name]
  (let [script-def (get-in @state [:script-list script-name])

        script-categories (:types script-def)
        script-io (:features script-def)

        filter-txt (get-in @state [:filter :txt])
        filter-categories (get-in @state [:filter :categories])
        filter-io (get-in @state [:filter :io])]
    (boolean
     (and
      (clojure.string/includes? script-name filter-txt)
      (or (empty? filter-categories)
          (every? script-categories filter-categories))
      (or (empty? filter-io)
          (every? script-io filter-io))))))
