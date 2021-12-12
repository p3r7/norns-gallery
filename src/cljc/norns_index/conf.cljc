(ns norns-index.conf)


;; CONF: SCRIPT I/O FEATURES

(def io-features
  {:grid {:values #{:grid_64 :grid_128}
          :catch-all-value :grid_any
          :is-required true}
   :arc {:values #{:arc}}
   :crow {:values #{:crow}}
   :jf {:values #{:jf}}
   :midi {:values #{:midi_in :midi_out}}
   :keyboard {:values #{:keyboard}}
   :mouse {:values #{:mouse}}
   })

(def ordered-filterable-io-features
  [:grid
   :arc
   :crow
   :midi])

(defn flattended-io-features []
  (set
   (mapcat
    (fn [[f f-props]]
      (let [std-vs (:values f-props)
            catch-all-v (:catch-all-value f-props)
            unspecified-v f             ; NB: current simplified implem
            all-vs (conj std-vs unspecified-v)
            all-vs (if catch-all-v
                     (conj all-vs catch-all-v)
                     all-vs)]
        all-vs))
    io-features)))

(def script-categories
  {"art" "art"
   "audio fx" "audio fx"
   "delays + loopers" "delays + loopers"
   "drums" "drums"
   "eduscript" "eduscript"
   "generative" "generative"
   "granulators" "granulators"
   "mods" "mods"
   ;; "jf" "jf"
   "samplers" "samplers"
   "sequencers" "sequencers"
   "synths" "synths"
   "utilities" "utilities"
   })

(def script-categories-order
  ["art"
   "audio fx"
   "delays + loopers"
   "drums"
   "eduscript"
   "generative"
   "granulators"
   "mods"
   ;; "jf"
   "samplers"
   "sequencers"
   "synths"
   "utilities"
   ])

(def script-connectivity-features
  {"grid" "grid"
   "arc" "arc"
   "crow" "crow"
   "jf" "just friend"
   "midi" "midi"
   "keyboard" "keyboard"
   "mouse" "mouse"}
  )
