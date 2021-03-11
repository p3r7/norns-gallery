(ns norns-index.views
  (:require
   [norns-index.utils.core :refer [member? remove-nils]]

   [norns-index.state :refer [state show-script?]]
   [norns-index.conf :as conf]))


(declare
 norns-script-features->icons
 ;; sub-views
 filter-panel
 script-panel screenshot feature
 script-category-section)



;; VIEW: MAIN

(defn main-view []
  [:div.main-view
   [filter-panel]
   (doall
    (map script-category-section conf/script-categories-order))])



;; VIEW: FILTER PANEL

(defn on-change-filter-grid [e]
  (swap! state assoc-in [:filter :io :grid :display] (keyword e.target.value)))

(defn on-change-filter-grid-size [e]
  ;; (js/console.info e.target.values)

  (.stopPropagation e)

  (if e.target.checked
    (swap! state update-in [:filter :io :grid :sizes] conj (keyword (str "grid_" e.target.value)))
    (swap! state update-in [:filter :io :grid :sizes] disj (keyword (str "grid_" e.target.value)))))

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

   ;; TODO: filter in/out/only, or only/exclude


   [:table
    [:thead
     [:tr
      [:th {:colSpan 3}
       "grid"]]]
    [:tbody
     [:tr
      [:td [:label.block
            [:input
             {:type "radio"
              :name "radio-grid"
              :value "optional"
              :style {:margin-left "0.5em"}
              :on-change on-change-filter-grid
              :checked (= :optional (get-in @state [:filter :io :grid :display]))
              }]
            [:span
             {:style {:margin-left "0.5em"}}
             "eventually"]]]
      [:td [:label.block
            [:input
             {:type "radio"
              :name "radio-grid"
              :value "only"
              :style {:margin-left "0.5em"}
              :on-change on-change-filter-grid
              :checked (= :only (get-in @state [:filter :io :grid :display]))
              }]
            [:span
             {:style {:margin-left "0.5em"}}
             "only"]
            ]]
      [:td [:label.block
            [:input
             {:type "radio"
              :name "radio-grid"
              :value "required"
              :style {:margin-left "0.5em"}
              :on-change on-change-filter-grid
              :checked (= :required (get-in @state [:filter :io :grid :display]))
              }]
            [:span
             {:style {:margin-left "0.5em"}}
             "only-required"]
            ]]
      [:td [:label.block
            [:input
             {:type "radio"
              :name "radio-grid"
              :value "no"
              :style {:margin-left "0.5em"}
              :on-change on-change-filter-grid
              :checked (= :no (get-in @state [:filter :io :grid :display]))
              }]
            [:span
             {:style {:margin-left "0.5em"}}
             "no"]
            ]]]
     [:tr
      [:td [:label.block
            [:input
             {:type "checkbox"
              :name "checkbox-grid-size"
              :value "128"
              :style {:margin-left "0.5em"}
              :on-change on-change-filter-grid-size
              :defaultChecked true
              ;; :checked (member? :grid-128 (get-in @state [:filter :io :grid :sizes]))
              }]
            [:span
             {:style {:margin-left "0.5em"}}
             "128"]
            ]]
      [:td [:label.block
            [:input
             {:type "checkbox"
              :name "checkbox-grid-size"
              :value "64"
              :style {:margin-left "0.5em"}
              :on-change on-change-filter-grid-size
              :defaultChecked true
              ;; :checked (member? :grid-64 (get-in @state [:filter :io :grid :sizes]))
              }]
            [:span
             {:style {:margin-left "0.5em"}}
             "64"]
            ]]
      [:td [:label.block
            [:input
             {:type "checkbox"
              :name "checkbox-grid-size"
              :value "any"
              :style {:margin-left "0.5em"}
              :on-change on-change-filter-grid-size
              :defaultChecked true
              ;; :checked (member? :grid-any (get-in @state [:filter :io :grid :sizes]))
              }]
            [:span
             {:style {:margin-left "0.5em"}}
             "arbitrary"]
            ]]

      ]]
    ]


   ])




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
     :arc "arc"}
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
          #(simple-feature->icon-maybe :kbd %1 %2)
          #(simple-feature->icon-maybe :mouse %1 %2)
          midi-feature->icon-maybe
          audio-feature->icon-maybe]))
   remove-nils))
