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
 row)



;; VIEW: MAIN

(defn main-view []
  [:div.container-fluid
   ;; [filter-panel]
   (doall
    (map row conf/script-categories-order))])



;; VIEW: FILTER PANEL

(defn filter-panel []
  [:div.row
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
        [:td
         {:colSpan 3}
         [:div.switch-toggle.switch-3.switch-candy

          [:input.switch-filter-out
           {:type "radio"
            :id (str "radio-" (name feature) "-no")
            :name (str "radio-" (name feature))
            :value "no"
            :style {:margin-left "0.5em"}
            :on-change on-change-visibility-fn
            :checked (= :no (get-in @state [:filter :io feature :display]))
            }]
          [:label.noselect
           {:for (str "radio-" (name feature) "-no")}
           "without"]

          [:input.switch-neutral
           {:type "radio"
            :id (str "radio-" (name feature) "-optional")
            :name (str "radio-" (name feature))
            :value "optional"
            :style {:margin-left "0.5em"}
            :on-change on-change-visibility-fn
            :checked (= :optional (get-in @state [:filter :io feature :display]))
            }]
          [:label.noselect
           {:for (str "radio-" (name feature) "-optional")}
           "n/a"]

          [:input.switch-filter-in
           {:type "radio"
            :id (str "radio-" (name feature) "-only")
            :name (str "radio-" (name feature))
            :value "only"
            :style {:margin-left "0.5em"}
            :on-change on-change-visibility-fn
            :checked (= :only (get-in @state [:filter :io feature :display]))
            }]
          [:label.noselect
           {:for (str "radio-" (name feature) "-only")}
           "only"]

          (when is-requirable
            [:span
             [:input.switch-filter-in-required
              {:type "radio"
               :id (str "radio-" (name feature) "-required")
               :name (str "radio-" (name feature))
               :value "required"
               :style {:margin-left "0.5em"}
               :on-change on-change-visibility-fn
               :checked (= :required (get-in @state [:filter :io feature :display]))
               }]
             [:label.noselect
              {:for (str "radio-" (name feature) "-required")}
              "only-required"]])

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

(defn row [script-category]
  (when-let [matched-scripts (-> (filter (fn [[script-name script-props]]
                                           (and
                                            (member? script-category (:types script-props))
                                            (show-script? script-name)
                                            )) (:script-list @state))
                                 keys
                                 sort
                                 seq)]
    ^{:key (str script-category)}
    [:div.row
     [:div.col-12
      [:h2 (get conf/script-categories script-category)]
     ]
     (doall
      (map #(script-panel script-category %) matched-scripts))]
     ))



;; VIEWS: SCRIPT PANEL

(defn script-panel [script-category script-name]
  (let [url (str "https://norns.community/" (get-in @state [:script-list script-name :path]))
        description (get-in @state [:script-list script-name :description])
        features (get-in @state [:script-list script-name :features])
        required-features (get-in @state [:script-list script-name :required-features])
        feature-icons (norns-script-features->icons features required-features)]
    ^{:key (str script-category "." script-name)}
    [:div.col-md-4
     [:div.script-panel
      {:on-click (fn [e]
                   (set! (.. js/window -top -location -href) url))}
      [screenshot script-name]
       [:h3 script-name]
       [:p description]
       [:ul.norns-feature-container
        (doall
         (map #(feature % script-category script-name) feature-icons))]
       ]]))

(defn screenshot [script-name]
  (let [author (get-in @state [:script-list script-name :author])]
    [:div.norns-screenshot-container
     [:img.img-norns-screenshot-default {:alt " " :src (str "https://norns.community/meta/scriptname.png")}]
     [:img.img-norns-screenshot {:alt " " :src (str "https://norns.community/community/" author "/" script-name ".png")}]
     ]))

(defn feature [feature-name & [script-category script-name]]
  ^{:key (str script-category "." script-name "." feature-name)}
  [:li {:class (str "feature-" feature-name)}
   [:img {:src (str "img/feature/" feature-name ".svg")}]])



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
     :keyboard "kbd"
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
    (simple-feature->icon :midi_out)

    ;; NB: unspecified fallback is current implem
    (member? :midi features)
    "midi"))

(defn audio-feature->icon-maybe [features required-features]
  (cond
    (every? #(member? % features) #{:audio_in :audio_out})
    "audio_io"

    (member? :audio_in features)
    (simple-feature->icon :audio_in)

    (member? :audio_out features)
    (simple-feature->icon :audio_out)

    ;; NB: unspecified fallback is current implem
    (member? :audio features)
    "audio"))

(defn grid-feature->icon-maybe [features required-features]
  (let [is-required (when required-features
                      (member? :grid required-features))]
    (cond
      (member? :grid_any features)
      (simple-feature->icon :grid_any is-required)

      (every? #(member? % features) #{:grid_64 :grid_128})
      (str "grid_64-128" (when is-required "!!"))

      (member? :grid_128 features)
      (simple-feature->icon :grid_128 is-required)

      ;; NB: unspecified fallback is current implem
      (member? :grid features)
      "grid")))

(defn norns-script-features->icons [features required-features]
  (->
   (map #(% features required-features)
        (reverse
         [grid-feature->icon-maybe
          #(simple-feature->icon-maybe :arc %1 %2)
          #(simple-feature->icon-maybe :crow %1 %2)
          #(simple-feature->icon-maybe :keyboard %1 %2)
          #(simple-feature->icon-maybe :mouse %1 %2)
          midi-feature->icon-maybe
          audio-feature->icon-maybe]))
   remove-nils))
