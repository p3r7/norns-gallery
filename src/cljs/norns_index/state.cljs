(ns norns-index.state
  (:require
   [norns-index.utils.core :refer [member?]]
   [reagent.core :as r]

   [norns-index.conf :as conf]))



;; STATE

(defonce state (r/atom {:script-list {}

                        :filter
                        {:txt ""
                         :io
                         {:grid {:display :optional
                                 :values #{:grid_128 :grid_64 :grid_any}}
                          :arc {:display :optional}
                          :crow {:display :optional}
                          :midi {:display :optional
                                 :values #{:midi_in :midi_out}}}
                         }}))



;; FILTER

(defn show-script-w-feature? [script-def feature]
  (let [
        ;; filters
        feature-display-f (get-in @state [:filter :io feature :display])
        feature-display-vals-f (get-in @state [:filter :io feature :values])

        ;; feature familly values
        feature-def (get conf/io-features feature)
        feature-std-vals (:values feature-def)
        feature-catch-all-val (:catch-all-value feature-def)
        feature-all-default-vals (if feature-catch-all-val
                                   (conj feature-std-vals feature-catch-all-val)
                                   feature-std-vals)

        ;; tested script
        script-features (:features script-def)
        script-matching-features-default (keep #(member? % script-features) feature-all-default-vals)
        script-matching-features-filtered (keep #(member? % script-features) feature-display-vals-f)
        script-matching (if (> (count feature-all-default-vals) 1)
                          script-matching-features-filtered
                          script-matching-features-default)
        script-requires (when (:required-features script-def)
                          (member? feature (:required-features script-def)))]

    (cond
      (empty? script-matching)
      (if (member? feature-display-f [:only :required])
        false
        true)

      (= feature-display-f :no)
      false

      (= feature-display-f :required)
      (if script-requires
        true
        false)

      :default                          ; feature-display-f = :only
      true
      )))


(defn show-script? [script-name]
  (let [script-def (get-in @state [:script-list script-name])
        script-features (:features script-def)
        filter-txt (get-in @state [:filter :txt])]
    (and
     (clojure.string/includes? script-name filter-txt)
     (every? #(show-script-w-feature? script-def %) [:grid :arc :crow :midi])))
  )
