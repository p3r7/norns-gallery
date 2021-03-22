(ns norns-index.conf)



;; CONF: SCRIPT I/O FEATURES

(def io-features
  {:grid {:values #{:grid_64 :grid_128}
          :catch-all-value :grid_any
          :is-required true}
   :arc {:values #{:arc}}
   :crow {:values #{:crow}}
   :midi {:values #{:midi_in :midi_out}}
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



;; CONF: SCRIPT INVENTORY

(def script-list
  {"awake" {:types [:sequencer]
            :features #{:audio_out :midi_out :grid_128}
            :required-features #{:grid}}
   "barcode" {:types [:looper]
              :features #{:audio_in :audio_out}}
   "changes" {:types [:cc]
              :features #{:midi_out}}
   "cranes" {:types [:looper]
             :features #{:audio_in :audio_out :grid_128}}
   ;; "downtown" {:types [:looper]}
   "drift" {:types [:sequencer]
            :features #{:audio_out :midi_out}}
   "drum_room" {:types [:sample-player]
                :features #{:audio_out :midi_in}
                :required-features #{:midi_in}}
   "euclydigons" {:types [:sequencer]
                  :features #{:audio_out :midi_out}}
   "flora" {:types [:sequencer]
            :features #{:audio_out :midi_out}}
   "fretwork" {:types [:sequencer]
               :features #{:audio_out :midi_out :grid_128}}
   "greyhole" {:types [:audio-effect]
               :features #{:audio_in :audio_out}}
   "gridstep" {:types [:sequencer]
               :features #{:audio_out :midi_out :grid_64 :grid_128}
               :required-features #{:grid}}
   "hachi" {:types [:synth]
            :features #{:audio_out}}
   "lissadron" {:types [:synth]
                :features #{:audio_out :midi_in}
                :encouraged-features #{:midi_in}}
   "loom" {:types [:sequencer]
           :features #{:grid_any :audio_out :midi_out}
           :required-features #{:grid}}
   "lost_futures" {:types [:synth]
                   :features #{:midi_in :audio_out}
                   :required-features #{:midi_in}}
   "mlr" {:types [:looper]
          :features #{:audio_in :audio_out :grid_128}
          :required-features #{:grid}}
   "molly_the_polly" {:types [:synth]
                      :features #{:midi_in :audio_out :grid_128}}
   "mouse" {:types [:sequencer]
            :features #{:kbd :mouse :audio_out :midi_out}}
   "oooooo" {:types [:looper]
              :features #{:audio_in :audio_out}}
   "orca" {:types [:tracker]
           :features #{:audio_in :audio_out :kbd :arc :crow :midi_out :grid_any}
           :required-features #{:kbd}}
   "ortf" {:types [:sample-player]
           :features #{:audio_out}}
   "passerby" {:types [:synth]
               :features #{:audio_out :midi_in :grid_128}}
   "pedalboard" {:types [:audio-effect]
                 :features #{:audio_in :audio_out}}
   "reels" {:types [:looper]
            :features #{:audio_in :audio_out}}
   "rpmate" {:types [:util]
             :features #{:audio_in :audio_out}}
   "rudiments" {:types [:synth]
                :features #{:grid_128 :audio_out}
                :encouraged-features #{:grid}}
   "sam" {:types [:sampler]
          :features #{:audio_in :audio_out}}
   "sines" {:types [:synth]
            :features #{:midi_in :audio_out}}
   "step" {:types [:sequencer]
           :features #{:grid_128 :audio_out}
           :required-features #{:grid}}
   "takt" {:types [:sequencer]
           :features #{:grid_128 :audio_out}
           :required-features #{:grid}}
   "timber_player" {:types [:sample-player]
                    :features #{:grid_128 :midi_in :audio_out}}
   "timeparty" {:types [:audio-effect]
                :features #{:audio_in :audio_out :grid}
                :required-features #{:grid}}
   "uvf" {:types [:sample-player]
          :features #{:audio_out}}
   "wrms" {:types [:looper]
           :features #{:audio_in :audio_out}}
   "yggrasil" {:types [:tracker]
               :features #{:kbd :midi_out :audio_out}
               :required-features #{:kbd :midi_out :audio_out}}
   }
  )

;; (def script-categories
;;   {:synth "Synths & Drones"
;;    :granular "Granular Processing"
;;    :sample-player "Sample Players"
;;    :sampler "Basic Samplers"
;;    :sequencer "Sequencers"
;;    :tracker "Trackers"
;;    :cc "Control Modulation Sources"
;;    :audio-effect "Audio Effets"
;;    :looper "Loopers, Live Samplers & Crazy Delays"
;;    :midi-effect "Midi Effets"
;;    :util "Utilities"
;;    })

;; (def script-categories-order
;;   [:synth
;;    :granular
;;    :sample-player
;;    :sampler
;;    :sequencer
;;    :tracker
;;    :cc
;;    :audio-effect
;;    :looper
;;    :midi-effect
;;    :util])

(def script-categories
  {"art" "art"
   "audio fx" "audio fx"
   "drums" "drums"
   "granulators" "granulators"
   "loopers + live samplers" "loopers + live samplers"
   "sequencers" "sequencers"
   "synths" "synths"
   "utilities" "utilities"
   })

(def script-categories-order
  ["art"
   "audio fx"
   "drums"
   "granulators"
   "loopers + live samplers"
   "sequencers"
   "synths"
   "utilities"
   ])
