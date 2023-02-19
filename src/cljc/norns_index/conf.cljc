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
   "audio fx" "audio fx"
   "delays + loopers" "delays + loopers"
   "drums" "drums"
   "eduscript" "eduscript"
   "games" "games"
   "generative" "generative"
   "granulators" "granulators"
   "mods" "mods"
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
   "games"
   "generative"
   "granulators"
   "mods"
   "samplers"
   "sequencers"
   "synths"
   "utilities"
   ])
