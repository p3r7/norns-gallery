(ns norns-index.views
  (:require
   [norns-index.utils.core :refer [member? remove-nils]]
   [norns-index.state :refer [state show-script?]]
   [norns-index.conf :as conf]))


(declare
 norns-script-features->icons
 ;; sub-views
 discover-script
 filter-panel
 filter-section-io-feature
 io-panel
 gallery-panel discover-gallery-panel
 screenshot feature
 simple-feature->icon
 row)



;; VIEW: MAIN

(defn main-view []
  [:div.container-fluid
   ;; [io-panel]
   [discover-script]])
   ;; (doall
   ;; (map row conf/script-categories-order))])


;; VIEW: I/O LEGEND

(defn io-panel []
  [:div.row
   [:div.col-12
    [:div.gallery-panel.container-fluid
      [:h2 "i/o icons"]
      [:ul.norns-feature-container.norns-feature-io.row
       (doall
        (map
         (fn [feature]
           (let [icon (simple-feature->icon (keyword feature))]
             ^{:key (str "io-feature-" feature)}
             [:li
              {:class (str "col-3 p-0 feature-" icon)}
              [:img {:src (str "img/feature/" icon ".svg") :alt (str feature " support")}]
              [:p feature]]))
         ["grid" "arc" "crow" "midi" "keyboard" "mouse"]))]]]])



;; VIEW: (RANDOM) FEATURED SCRIPT

(defn discover-script []
  (when-let [random-script-name (rand-nth (keys (:script-list @state)))]
    [:div.row
      ;; [:div.col-12
      ;; [:h1 "discover"]]
      ;; todo: (take 4 (shuffle coll))
      [discover-gallery-panel random-script-name]
      [discover-gallery-panel random-script-name]
      [discover-gallery-panel random-script-name]
      [discover-gallery-panel random-script-name]]))


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
      [:h1 (get conf/script-categories script-category)]
      ]
     (doall
      (map #(gallery-panel script-category %) matched-scripts))]
    ))



;; VIEWS: SCRIPT PANEL

(defn gallery-panel [script-category script-name]
  (let [url (str "https://norns.community/" (get-in @state [:script-list script-name :path]))
        description (get-in @state [:script-list script-name :description])
        features (get-in @state [:script-list script-name :features])
        required-features (get-in @state [:script-list script-name :required-features])
        feature-icons (norns-script-features->icons features required-features)]
    ^{:key (str script-category "." script-name)}
    [:div.col-md-6.col-lg-4
     [:div.gallery-panel
      {:on-click (fn [e]
                   (set! (.. js/window -top -location -href) url))}
      [screenshot script-name]
      [:h3 script-name]
      [:p description]
      [:ul.norns-feature-container
       (doall
        (map #(feature % script-category script-name) feature-icons))]
      ]]))

(defn discover-gallery-panel [script-name]
  (let [url (str "https://norns.community/" (get-in @state [:script-list script-name :path]))
        description (get-in @state [:script-list script-name :description])
        author (get-in @state [:script-list script-name :author])
        author-url (str "https://norns.community/en/authors/" author)
        features (get-in @state [:script-list script-name :features])
        required-features (get-in @state [:script-list script-name :required-features])
        feature-icons (norns-script-features->icons features required-features)]
    [:div.col-md-6.col-lg-4
     [:div.gallery-panel.container-fluid
      {:on-click (fn [e]
                   (set! (.. js/window -top -location -href) url))}
      [:div.row
        [:div.col-6
          [screenshot script-name]
          [:ul.norns-feature-container
           (doall
            (map #(feature % "discover" script-name) feature-icons))]]
        [:div.col-6
          [:h3 script-name]
          [:p "by " [:a {:href author-url} (str "@" author)]]
          [:p description]]]]]))

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
     :midi "midi"
     :audio_in "audio_i"
     :audio_out "audio_o"
     :audio "audio"
     :grid_128 "grid_128"
     :grid_any "grid_any"
     :grid "grid"
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
    (simple-feature->icon :midi)))

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
    (simple-feature->icon :audio)))

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
      (simple-feature->icon :grid is-required))))

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
