(ns norns-index.app
  (:require
   ;; core
   [clojure.edn :as edn]
   [goog.dom :as gdom]
   [norns-index.utils.core :refer [member?]]

   ;; react
   [reagent.core :as r]
   [reagent.dom :as rdom]))


(declare mount-root mount-app-element app-element
         ;; views
         main-view script-panel screenshot
         script-category-section)



;; CONF

(def screenshot-scale 3)
(def screenshot-padding 10)

(def script-list
  {"awake" {:types [:sequencer]}
   "barcode" {:types [:looper]}
   "changes" {:types [:cc]}
   "cranes" {:types [:looper]}
   ;; "downtown" {:types [:looper]}
   "drift" {:types [:sequencer]}
   "drum_room" {:types [:sample-player]}
   "euclydigons" {:types [:sequencer]}
   "flora" {:types [:sequencer]}
   "fretwork" {:types [:sequencer]}
   "greyhole" {:types [:audio-effect]}
   "gridstep" {:types [:sequencer]}
   "hachi" {:types [:synth]}
   "lissadron" {:types [:synth]}
   "loom" {:types [:sequencer]}
   "lost_futures" {:types [:synth]}
   "mlr" {:types [:looper]}
   "molly_the_polly" {:types [:synth]}
   "mouse" {:types [:sequencer]}
   "ooooooo" {:types [:looper]}
   "orca" {:types [:tracker]}
   "ortf" {:types [:sample-player]}
   "passerby" {:types [:synth]}
   "pedalboard" {:types [:audio-effect]}
   "reels" {:types [:looper]}
   "rpmate" {:types [:util]}
   "rudiments" {:types [:synth]}
   "sam" {:types [:sampler]}
   "sines" {:types [:synth]}
   "step" {:types [:sequencer]}
   "takt" {:types [:sequencer]}
   "timber_player" {:types [:sample-player]}
   "timeparty" {:types [:audio-effect]}
   "uvf" {:types [:sample-player]}
   "wrms" {:types [:looper]}
   "yggrasil" {:types [:tracker]}
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



;; HELPERS

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



;; VIEWS

(defn main-view []
  [:div
   [:input {:type "text"
            :on-change (fn [e]
                         (swap! state assoc-in [:filter :txt] e.target.value))}]
   (doall
    (map script-category-section (keys script-categories)))])


(defn script-category-section [script-category]
  (when-let [matched-scripts (keys (filter (fn [[script-name script-props]]
                                             (and
                                              (member? script-category (:types script-props))
                                              (clojure.string/includes? script-name (get-in @state [:filter :txt])))) script-list))]
    ^{:key (str script-category)}
    [:div.script-category-section
     [:h2 (get script-categories script-category)]
     [:div.flex.flex-wrap.script-panels-container
      (doall
       (map #(script-panel script-category %) matched-scripts))]]))

(defn script-panel [script-category script-name]
  ^{:key (str script-category "." script-name)}
  [:div.script-panel-container
   [:div.script-panel
    [screenshot script-name]
    [:div
     [:p.script-title (clojure.string/upper-case script-name)]]]])

(defn screenshot [script-name]
  [:div {:style {:background-color "black"
                 :width (str (+ (* screenshot-scale 128) (* 2 screenshot-padding)) "px")
                 :height (str (+ (* screenshot-scale 64) (* 2 screenshot-padding)) "px")
                 :padding (str screenshot-padding "px")}}
   [:img.img-norns-screenshot {:src (str "img/screenshot/" script-name ".png")
                               :style {:width (str (* screenshot-scale 128) "px")
                                       :height "auto"}}]])
