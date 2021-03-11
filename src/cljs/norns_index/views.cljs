(ns norns-index.views
  (:require
   [norns-index.utils.core :refer [member? remove-nils]]

   [norns-index.state :refer [state show-script?]]
   [norns-index.conf :as conf]))


(declare
 norns-script-features->icons
 ;; sub-views
 filter-panel
 filter-section-io-feature
 script-panel screenshot feature
 script-category-section)



;; VIEW: MAIN

(defn main-view []
  [:div.main-view
   [filter-panel]
   (doall
    (map script-category-section conf/script-categories-order))])



;; VIEW: FILTER PANEL

(defn filter-panel []
  [:div.script-category-section
   [:h2 "Filter"]
   [:label.block
    [:span "Name"]
    [:input
     {:type "text"
      :style {:margin-left "0.5em"}
      :on-change (fn [e]
                   (swap! state assoc-in [:filter :txt] e.target.value))}]]
   [:div
    (doall
     (map
      filter-section-io-feature
      conf/ordered-filterable-io-features))]])

(defn filter-section-io-feature [feature]
  (let [feature-def (get conf/io-features feature)

        feature-std-vals (:values feature-def)
        feature-catch-all-val (:catch-all-value feature-def)
        feature-all-default-vals (if feature-catch-all-val
                                   (conj feature-std-vals feature-catch-all-val)
                                   feature-std-vals)

        is-requirable (:is-required feature-def)

        on-change-visibility-fn (fn [e]
                                  (swap! state assoc-in [:filter :io feature :display] (keyword e.target.value)))
        on-change-value-fn (fn [e]
                             (.stopPropagation e)
                             (if e.target.checked
                               (swap! state update-in [:filter :io feature :values] conj (keyword e.target.value))
                               (swap! state update-in [:filter :io feature :values] disj (keyword e.target.value))))]
    ^{:key (str "filter-section-io-" (name feature))}
    [:div
     [:table
      [:thead
       [:tr
        [:th {:colSpan 3}
         (name feature)]]]
      [:tbody
       ;; REVIEW: filter in/out/only, or only/exclude
       [:tr
        [:td [:label.block
              [:input
               {:type "radio"
                :name (str "radio-" (name feature))
                :value "optional"
                :style {:margin-left "0.5em"}
                :on-change on-change-visibility-fn
                :checked (= :optional (get-in @state [:filter :io feature :display]))
                }]
              [:span
               {:style {:margin-left "0.5em"}}
               "eventually"]]]
        [:td [:label.block
              [:input
               {:type "radio"
                :name (str "radio-" (name feature))
                :value "only"
                :style {:margin-left "0.5em"}
                :on-change on-change-visibility-fn
                :checked (= :only (get-in @state [:filter :io feature :display]))
                }]
              [:span
               {:style {:margin-left "0.5em"}}
               "only"]
              ]]
        (when is-requirable
          [:td [:label.block
                [:input
                 {:type "radio"
                  :name (str "radio-" (name feature))
                  :value "required"
                  :style {:margin-left "0.5em"}
                  :on-change on-change-visibility-fn
                  :checked (= :required (get-in @state [:filter :io feature :display]))
                  }]
                [:span
                 {:style {:margin-left "0.5em"}}
                 "only-required"]
                ]])
        [:td [:label.block
              [:input
               {:type "radio"
                :name (str "radio-" (name feature))
                :value "no"
                :style {:margin-left "0.5em"}
                :on-change on-change-visibility-fn
                :checked (= :no (get-in @state [:filter :io feature :display]))
                }]
              [:span
               {:style {:margin-left "0.5em"}}
               "no"]
              ]]]
       [:tr
        (when (> (count feature-all-default-vals) 1)
          (doall
           (map
            (fn [feature-v]
              (let [label (clojure.string/replace-first (name feature-v) (str (name feature) "_") "")]
                ^{:key (str "filter-block-" (name feature-v))}
                [:td [:label.block
                      [:input
                       {:type "checkbox"
                        :name (str "checkbox-" (name feature) "-value")
                        :value (name feature-v)
                        :style {:margin-left "0.5em"}
                        :on-change on-change-value-fn
                        :defaultChecked true}]
                      [:span
                       {:style {:margin-left "0.5em"}}
                       label]]]))
            feature-all-default-vals)))]]]]))



;; VIEW: SCRIPT CATEGORY

(defn script-category-section [script-category]
  (when-let [matched-scripts (-> (filter (fn [[script-name script-props]]
                                           (and
                                            (member? script-category (:types script-props))
                                            (show-script? script-name)
                                            )) conf/script-list)
                                 keys
                                 sort
                                 seq)]
    ^{:key (str script-category)}
    [:div.script-category-section
     [:h2 (get conf/script-categories script-category)]
     [:div.flex.flex-wrap.script-panels-container
      (doall
       (map #(script-panel script-category %) matched-scripts))]]))



;; VIEWS: SCRIPT PANEL

(defn script-panel [script-category script-name]
  (let [features (get-in conf/script-list [script-name :features])
        required-features (get-in conf/script-list [script-name :required-features])
        feature-icons (norns-script-features->icons features required-features)]
    ^{:key (str script-category "." script-name)}
    [:div.script-panel-container
     ;; {:style {:display (if (show-script? script-name) "block" "none")}} ;NB: this might be an optim, less diffs between React & actual DOM
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



;; HELPERS - I/O FEATURES ICONS

(defn simple-feature->icon [feature & [is-required]]
  (str
   (get
    {:midi_in "midi_i"
     :midi_out "midi_o"
     :audio_in "audio_i"
     :audio_out "audio_o"
     :grid_128 "grid_128"
     :grid_any "grid_any"
     :kbd "kbd"
     :mouse "mouse"
     :arc "arc"
     :crow "crow"}
    feature)
   (when is-required "!!"))
  )

(defn simple-feature->icon-maybe [search features required-features]
  (when (member? search features)
    (simple-feature->icon search)))

(defn midi-feature->icon-maybe [features required-features]
  (cond
    (every? #(member? % features) #{:midi_in :midi_out})
    "midi_io"

    (member? :midi_in features)
    (simple-feature->icon :midi_in)

    (member? :midi_out features)
    (simple-feature->icon :midi_out)))

(defn audio-feature->icon-maybe [features required-features]
  (cond
    (every? #(member? % features) #{:audio_in :audio_out})
    "audio_io"

    (member? :audio_in features)
    (simple-feature->icon :audio_in)

    (member? :audio_out features)
    (simple-feature->icon :audio_out)))

(defn grid-feature->icon-maybe [features required-features]
  (let [is-required (when required-features
                      (member? :grid required-features))]
    (cond
      (member? :grid_any features)
      (simple-feature->icon :grid_any is-required)

      (every? #(member? % features) #{:grid_64 :grid_128})
      (str "grid_64-128" (when is-required "!!"))

      (member? :grid_128 features)
      (simple-feature->icon :grid_128 is-required))))


(defn norns-script-features->icons [features required-features]
  (->
   (map #(% features required-features)
        (reverse
         [grid-feature->icon-maybe
          #(simple-feature->icon-maybe :arc %1 %2)
          #(simple-feature->icon-maybe :crow %1 %2)
          #(simple-feature->icon-maybe :kbd %1 %2)
          #(simple-feature->icon-maybe :mouse %1 %2)
          midi-feature->icon-maybe
          audio-feature->icon-maybe]))
   remove-nils))
