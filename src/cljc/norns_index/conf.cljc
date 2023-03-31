(ns norns-index.conf)



;; CONF: SCRIPT I/O FEATURES

(def script-io-features
  {"grid" "grid"
   "arc" "arc"
   "crow" "crow"
   "jf" "just friend"
   "midi" "midi"
   "16n" "16n"
   "keyboard" "keyboard"
   "mouse" "mouse"})

(def script-io-features-order
  [:grid :arc
   :crow :jf
   :midi :16n
   :keyboard :mouse])

(def io-feature->icon
  {:midi "midi"
   :grid "grid"
   :keyboard "kbd"
   :mouse "mouse"
   :arc "arc"
   :crow "crow"
   :jf "jf"
   :16n "16n"})



;; CONF: SCRIPT CATEGORIES

(def script-categories
  {"art" "art"
   "audio_fx" "audio fx"
   "delay" "delays"
   "looper" "loopers"
   "drum" "drums"
   "drone" "drone"
   "eduscript" "eduscript"
   "game" "games"
   "generative" "generative"
   "granulator" "granulators"
   "mod" "mods"
   "sampler" "samplers"
   "sequencer" "sequencers"
   "synth" "synths"
   "utility" "utilities"
   })

(def script-categories-order
  ["art"
   "audio_fx"
   "delay"
   "looper"
   "drum"
   "drone"
   "eduscript"
   "game"
   "generative"
   "granulator"
   "mod"
   "sampler"
   "sequencer"
   "synth"
   "utility"
   ])
