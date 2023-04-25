(ns norns-index.views
  (:require
   [taoensso.encore :as encore]
   [norns-index.utils.core :refer [member? remove-nils take-n-distinct-rand]]
   [norns-index.state :refer [state show-script?]]
   [norns-index.conf :as conf]))

(declare
 script-io-features->icons
 ;; sub-views
 filter-panel
 filter-section-io-feature
 single-script
 screenshot
 feature
 row-by-category)


;; ASSETS PATHS

(def baseurl "https://vaporwavemall.com/")

(defn author->author-url [author]
  (str baseurl "author#" author))

(defn script-name->screenshot [script-name] (str baseurl "assets/screenshots/" script-name ".png"))

(defn script-name->url [script-name]
  (str baseurl script-name))

(def io-icon-folder (str baseurl "assets/icons/"))

(def is-static (atom false))


;; VIEW: MAINS

(defn main-view-all []
  [:div.main-view-all

   [:div.filters
    (when-not @is-static
      [filter-panel])]
   (encore/when-let [script-names (-> (:script-list @state)
                                      keys
                                      sort
                                      seq)
                     ;; filtered-script-names (filter show-script? script-names)
                     ]
     [:div.scripts
      (doall
       (map #(single-script %) script-names))])])

(defn main-view-all-categorized []
  (doall
    (map #(row-by-category % :show-header true) conf/script-categories-order)))


;; VIEW: FILTER FORM

(defn categories-filter-form []
  [:div.categories-filter-form
   (doall
    (map
     (fn [f]
       (let [checkbox-id (str "checkbox-category-" f)]
         ^{:key (str "checkbox-category-" f)}
         [:div
          [:input
           {:type "checkbox"
            ;; :autocomplete "off"
            :id checkbox-id
            ;; :checked ((keyword f) (get-in @state [:filter :categories]))
            :on-change (fn [e]

                         ;; (if e.target.checked
                         ;;   (js/console.info (str f " is ON"))
                         ;;   (js/console.info (str f " is OFF")))

                         (.stopPropagation e)
                         (if e.target.checked
                           (swap! state update-in [:filter :categories] conj (keyword f))
                           (swap! state update-in [:filter :categories] disj (keyword f))))
            }]
          [:label
           {:for checkbox-id} f]]))
     (map name conf/script-categories-order)))])

(defn io-filter-form []
  [:div.io-filter-form
   (doall
    (map
     (fn [f]
       (let [checkbox-id (str "checkbox-io-" f)]
         ^{:key (str "checkbox-io-" f)}
         [:div
          [:input
           {:type "checkbox"
            ;; :autocomplete "off"
            :id checkbox-id
            ;; :checked ((keyword f) (get-in @state [:filter :io]))
            :on-change (fn [e]

                         ;; (if e.target.checked
                         ;;   (js/console.info (str f " is ON"))
                         ;;   (js/console.info (str f " is OFF")))

                         (.stopPropagation e)
                         (if e.target.checked
                           (swap! state update-in [:filter :io] conj (keyword f))
                           (swap! state update-in [:filter :io] disj (keyword f))))
            }]
          [:label
           {:for checkbox-id} f]]))
     (map name conf/script-io-features-order)))])

(defn filter-panel []
  [:div.filter-panel
   [categories-filter-form]
   [io-filter-form]]
  )


;; VIEW: SCRIPT CATEGORY

(defn row-by-category [script-category & {:keys [show-header]}]
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
     (when show-header
       [:div.col-12
        [:h1 (get conf/script-categories script-category)]])
     (doall
      (map #(single-script %) matched-scripts))]))



;; VIEWS: SCRIPT PANEL

(defn single-script [script-name]
  (let [url (script-name->url script-name)
        script (get-in @state [:script-list script-name])
        description (:description script)
        authors (:author script)
        author-links (map #(vec (list % (author->author-url %))) authors)
        features (:features script)
        feature-icons (script-io-features->icons features)

        categories (:types script)
        ]
    ^{:key (str script-name)}
    [:div
     {:class
      ;; (map name (into categories features))
      (when-not (show-script? script-name) "hidden")
      }
     [:div.single-script
      [:a.single-script-wrapper-link {:href url} script-name
        [screenshot script-name]
        [:ul.feature
          (doall
          (map #(feature % "random" script-name) feature-icons))]
        [:a.script-name {:href url} script-name]
        [:p "by " (map (fn [[author author-url]]
                          ^{:key (str author)}
                          [:span [:a {:href author-url} (str "@" author)] [:br]])
                        author-links)]
        [:p description]]]
    ]))

(defn screenshot [script-name]
    [:div.screenshot
     [:img {:alt " " :src (script-name->screenshot script-name)}]
     ])

(defn feature [feature-name & [script-category script-name]]
  ^{:key (str script-category "." script-name "." feature-name)}
  [:li {:class (str "feature-" feature-name)}
   [:img {:src (str io-icon-folder feature-name ".svg") :alt feature-name}]])


;; HELPERS - I/O FEATURES ICONS

(defn script-io-features->icons [features]
  (let [ordered-script-features (remove-nils (map features conf/script-io-features-order))]
    (->
     (map #(% conf/io-feature->icon) ordered-script-features)
     reverse)))
