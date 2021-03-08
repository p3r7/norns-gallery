(ns norns-index.app
  (:require
   ;; core
   [clojure.edn :as edn]
   [goog.dom :as gdom]
   [norns-index.utils.core :refer [member? remove-nils]]

   ;; react
   [reagent.core :as r]
   [reagent.dom :as rdom]))


(declare mount-root mount-app-element app-element
         ;; views
         main-view script-panel screenshot feature
         script-category-section)



;; CONF

(def script-list
  {"awake" {:types [:sequencer]
            :features #{:audio_out :midi_out :grid_128}}
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
                :features #{:audio_out :midi_in}}
   "euclydigons" {:types [:sequencer]
                  :features #{:audio_out :midi_out}}
   "flora" {:types [:sequencer]
            :features #{:audio_out :midi_out}}
   "fretwork" {:types [:sequencer]
               :features #{:audio_out :midi_out :grid_128}}
   "greyhole" {:types [:audio-effect]
               :features #{:audio_in :audio_out}}
   "gridstep" {:types [:sequencer]
               :features #{:audio_out :midi_out :grid_64 :grid_128}}
   "hachi" {:types [:synth]
            :features #{:audio_out}}
   "lissadron" {:types [:synth]
                :features #{:audio_out :midi_in}}
   "loom" {:types [:sequencer]
           :features #{:grid_any :audio_out :midi_out}}
   "lost_futures" {:types [:synth]
                   :features #{:midi_in :audio_out}}
   "mlr" {:types [:looper]
          :features #{:audio_in :audio_out :grid_128}}
   "molly_the_polly" {:types [:synth]
                      :features #{:midi_in :audio_out :grid_128}}
   "mouse" {:types [:sequencer]
            :features #{:kbd :audio_out :midi_out}}
   "ooooooo" {:types [:looper]
              :features #{:audio_in :audio_out}}
   "orca" {:types [:tracker]
           :features #{:audio_in :audio_out :kbd :arc :midi_out :grid_any}}
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
                :features #{:grid_128 :audio_out}}
   "sam" {:types [:sampler]
          :features #{:audio_in :audio_out}}
   "sines" {:types [:synth]
            :features #{:midi_in :audio_out}}
   "step" {:types [:sequencer]
           :features #{:grid_128 :audio_out}}
   "takt" {:types [:sequencer]
           :features #{:grid_128 :audio_out}}
   "timber_player" {:types [:sample-player]
                    :features #{:grid_128 :midi_in :audio_out}}
   "timeparty" {:types [:audio-effect]
                :features #{:audio_in :audio_out}}
   "uvf" {:types [:sample-player]
          :features #{:audio_out}}
   "wrms" {:types [:looper]
           :features #{:audio_in :audio_out}}
   "yggrasil" {:types [:tracker]
               :features #{:kbd :midi_out :audio_out}}
   }
  )

(def script-categories
  {:synth "Synths & Drones"
   :granular "Granular Processing"
   :sample-player "Sample Players"
   :sampler "Basic Samplers"
   :sequencer "Sequencers"
   :tracker "Trackers"
   :cc "Control Modulation Sources"
   :audio-effect "Audio Effets"
   :looper "Loopers, Live Samplers & Crazy Delays"
   :midi-effect "Midi Effets"
   :util "Utilities"
   })

(def script-categories-order
  [:synth
   :granular
   :sample-player
   :sampler
   :sequencer
   :tracker
   :cc
   :audio-effect
   :looper
   :midi-effect
   :util])



;; STATE

(def state (r/atom {:filter
                    {:txt ""
                     }}))


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
  (mount-app-element [main-view]))



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



;; HELPERS - NORNS SCRIPT FEATURES

(defn simple-feature->icon [feature]
  (get
   {:midi_in "midi_i"
    :midi_out "midi_o"
    :audio_in "audio_i"
    :audio_out "audio_o"
    :grid_128 "grid_128"
    :grid_any "grid_any"
    :kbd "kbd"
    :arc "arc"}
   feature))

(defn simple-feature->icon-maybe [search features]
  (when (member? search features)
    (simple-feature->icon search)))

(defn midi-feature->icon-maybe [features]
  (cond
    (every? #(member? % features) #{:midi_in :midi_out})
    "midi_io"

    (member? :midi_in features)
    (simple-feature->icon :midi_in)

    (member? :midi_out features)
    (simple-feature->icon :midi_out)))

(defn audio-feature->icon-maybe [features]
  (cond
    (every? #(member? % features) #{:audio_in :audio_out})
    "audio_io"

    (member? :audio_in features)
    (simple-feature->icon :audio_in)

    (member? :audio_out features)
    (simple-feature->icon :audio_out)))

(defn grid-feature->icon-maybe [features]
  (cond
    (member? :grid_any features)
    (simple-feature->icon :grid_any)

    (every? #(member? % features) #{:grid_64 :grid_128})
    "grid_64-128"

    (member? :grid_128 features)
    (simple-feature->icon :grid_128)))


(defn norns-script-features->icons [features]
  (->
   (map #(% features) [midi-feature->icon-maybe
                       audio-feature->icon-maybe
                       grid-feature->icon-maybe
                       #(simple-feature->icon-maybe :kbd %)
                       #(simple-feature->icon-maybe :arc %)])
   remove-nils
   set))



;; VIEWS

(defn main-view []
  [:div.main-view
   [:div.script-category-section
    [:h2 "Filter"]
    [:label.block
     [:span "Name"]
     [:input
      {:type "text"
       :style {:margin-left "0.5em"}
       :on-change (fn [e]
                    (swap! state assoc-in [:filter :txt] e.target.value))}]]]
   (doall
    (map script-category-section script-categories-order))])


(defn script-category-section [script-category]
  (when-let [matched-scripts (-> (filter (fn [[script-name script-props]]
                                           (and
                                            (member? script-category (:types script-props))
                                            (clojure.string/includes? script-name (get-in @state [:filter :txt])))) script-list)
                                 keys
                                 sort
                                 seq)]
    ^{:key (str script-category)}
    [:div.script-category-section
     [:h2 (get script-categories script-category)]
     [:div.flex.flex-wrap.script-panels-container
      (doall
       (map #(script-panel script-category %) matched-scripts))]]))

(defn script-panel [script-category script-name]
  (let [features (get-in script-list [script-name :features])
        feature-icons (norns-script-features->icons features)]
    ^{:key (str script-category "." script-name)}
    [:div.script-panel-container
     [:div.script-panel
      [screenshot script-name]
      [:div
       [:p.script-title (clojure.string/upper-case script-name)]
       [:div.flex.flex-wrap
        (doall
         (map #(feature % script-category script-name) feature-icons))]
       ]]]))

(defn screenshot [script-name]
  [:div.norns-screenshot-container
   [:img.img-norns-screenshot {:src (str "img/screenshot/" script-name ".png")}]])

(defn feature [feature-name & [script-category script-name]]
  ^{:key (str script-category "." script-name "." feature-name)}
  [:div.norns-feature-container
   [:img.img-norns-feature {:src (str "img/feature/" feature-name ".svg")}]])
