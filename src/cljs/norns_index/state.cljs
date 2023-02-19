(ns norns-index.state
  (:require
   [reagent.core :as r]

   [norns-index.conf :as conf]))



;; STATE

(defonce state (r/atom {:script-list {}

                        :filter
                        {:txt ""
                         :io (set conf/io-features)
                         }}))



;; FILTER

(defn show-script? [script-name]
  (let [script-def (get-in @state [:script-list script-name])
        script-features (:features script-def)
        filter-txt (get-in @state [:filter :txt])]
    (and
     (clojure.string/includes? script-name filter-txt)
     (some #(% script-features) conf/io-features))))
