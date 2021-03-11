(ns norns-index.state
  (:require
   [norns-index.utils.core :refer [member?]]
   [reagent.core :as r]

   [norns-index.conf :as conf]))



;; STATE

(def state (r/atom {:filter
                    {:txt ""
                     :io
                     {:grid {:display :optional
                             :values #{:grid_128 :grid_64 :grid_any}}}
                     {:arc {:display :optional}}
                     {:crow {:display :optional}}
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
        feature-all-default-vals (conj feature-std-vals feature-catch-all-val)

        ;; tested script
        script-features (:features script-def)
        script-matching-features-default (keep #(member? % script-features) feature-all-default-vals)
        script-matching-features-filtered (keep #(member? % script-features) feature-display-vals-f)
        script-requires (when (:required-features script-def)
                          (member? feature (:required-features script-def)))]

    (cond
      (empty? script-matching-features-filtered)
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
      )

    )
  )


(defn show-script? [script-name]
  (let [script-def (get conf/script-list script-name)
        script-features (:features script-def)
        filter-txt (get-in @state [:filter :txt])]
    (and
     (clojure.string/includes? script-name filter-txt)
     (show-script-w-feature? script-def :grid)
     ;; (show-script-w-feature? script-def :arc)

     ))
  )
