(ns norns-index.views
  (:require
   [norns-index.utils.core :refer [member? remove-nils take-n-distinct-rand]]
   [norns-index.state :refer [state show-script?]]
   [norns-index.conf :as conf]))

(declare
 script-io-features->icons
 ;; sub-views
 random-scripts
 filter-panel
 filter-section-io-feature
 io-panel
 gallery-panel
 screenshot
 feature
 row-by-category row-by-feature row-by-author)



;; ASSETS PATHS

;; old implem
;; (defn script-name->screenshot [script-name]
;;   (let [author (get-in @state [:script-list script-name :author])]
;;     (str "https://norns.community/community/" author "/" script-name ".png")))

(defn author->author-url [author]
  ;; (str "https://norns.community/en/authors/" author)
  (str "https://vaporwavemall.com/author#" author))

(defn script-name->screenshot [script-name] (str "https://vaporwavemall.com/assets/screenshots/" script-name ".png"))

(defn script-name->url [script-name]
  ;; (str "https://norns.community/" (get-in @state [:script-list script-name :path]))
  (str "https://vaporwavemall.com/" script-name))

;; (def io-icon-folder "img/feature/")
(def io-icon-folder "https://vaporwavemall.com/assets/icons/")

(def wide-screen-support false)



;; VIEW: MAINS

(defn main-view-all []
  [:div.container-fluid
   (doall
    (map #(row-by-category % :show-header true) conf/script-categories-order))])

(defn main-view-single-category [category-name]
  [:div.container-fluid
   [row-by-category category-name]])

(defn main-view-single-connectivity-feature [feature-name]
  [:div.container-fluid
   [row-by-feature feature-name]])

(defn main-view-single-author [author]
  [:div.container-fluid
   [row-by-author author]])

(defn main-view-random [nb]
  [:div.container-fluid
   [random-scripts nb]])



;; VIEW: I/O LEGEND

(defn io-panel []
  [:div.row
   [:div.col-12
    [:div.gallery-panel.container-fluid
     [:h2 "i/o icons"]
     [:ul.norns-feature-container.norns-feature-io.row
      (doall
       (map
        (fn [f]
          (let [icon ((keyword f) conf/io-feature->icon)]
            ^{:key (str "io-feature-" f)}
            [:li
             {:class (str "col-3 p-0 feature-" icon)}
             [:img {:src (str io-icon-folder icon ".svg") :alt (str f " support")}]
             [:p (conf/script-io-features f)]]))
        (map keyword conf/script-io-features-order)))]]]])



;; VIEW: (RANDOM) FEATURED SCRIPT

(defn random-scripts [nb]
  (let [script-list (:script-list @state)]
    (when (< 0 (count script-list))
      (let [random-script-names (-> (take-n-distinct-rand nb (keys script-list))
                                    vec
                                    shuffle)]
        [:div.row
         (doall
          (map
           (fn [script-name]
             ^{:key (str "random-" script-name)}
             [gallery-panel script-name])
           random-script-names))]))))



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
      (map #(gallery-panel %) matched-scripts))]))



;; VIEW: SCRIPT GROUPED BY CONNECTIVITY

(defn row-by-feature [feature-name & {:keys [show-header]}]
  (when-let [matched-scripts (-> (filter (fn [[script-name script-props]]
                                           (and
                                            (member? (keyword feature-name) (:features script-props))
                                            (show-script? script-name)
                                            )) (:script-list @state))
                                 keys
                                 sort
                                 seq)]
    ^{:key (str feature-name)}
    [:div.row
     (when show-header
       [:div.col-12
        [:h1 (get conf/script-io-features feature-name)]])
     (doall
      (map #(gallery-panel %) matched-scripts))]))



;; VIEW: SCRIPT AUTHOR

(defn row-by-author [author]
  (when-let [matched-scripts (-> (filter (fn [[script-name script-props]]
                                           (and
                                            (member? author (:author script-props))
                                            (show-script? script-name)
                                            )) (:script-list @state))
                                 keys
                                 sort
                                 seq)]
    ^{:key (str author)}
    [:div.row
     (doall
      (map #(gallery-panel %) matched-scripts))]))


;; VIEWS: SCRIPT PANEL

(defn gallery-panel [script-name]
  (let [url (script-name->url script-name)
        description (get-in @state [:script-list script-name :description])
        authors (get-in @state [:script-list script-name :author])
        author-links (map #(vec (list % (author->author-url %))) authors)
        features (get-in @state [:script-list script-name :features])
        feature-icons (script-io-features->icons features)]
    ^{:key (str script-name)}
    [:div.d-block.col-md-6.col-sm-12
     {:class (when wide-screen-support "col-xl-3")}
     [:div.gallery-panel.container-fluid
      {:on-click (fn [e]
                   (if (or e.ctrlKey e.metaKey)
                     (js/window.open url "_blank")
                     (set! (.. js/window -top -location -href) url)))}
      [:div.row
       [:div.col-6
        [screenshot script-name]
        [:ul.norns-feature-container
         (doall
          (map #(feature % "random" script-name) feature-icons))]]
       [:div.col-6
        [:h3.script-name script-name]
        [:p "by " (map (fn [[author author-url]]
                         ^{:key (str author)}
                         [:span [:a {:href author-url} (str "@" author)] [:br]])
                       author-links)]
        [:p description]]]]
     ;; ]
     ]))

(defn screenshot [script-name]
    [:div.norns-screenshot-container
     [:img.img-norns-screenshot-default {:alt " " :src (str "https://norns.community/meta/scriptname.png")}]
     [:img.img-norns-screenshot {:alt " " :src (script-name->screenshot script-name)}]
     ])

(defn feature [feature-name & [script-category script-name]]
  ^{:key (str script-category "." script-name "." feature-name)}
  [:li {:class (str "feature-" feature-name)}
   [:img {:src (str io-icon-folder feature-name ".svg")}]])



;; HELPERS - I/O FEATURES ICONS

(defn script-io-features->icons [features]
  (let [ordered-script-features (remove-nils (map features conf/script-io-features-order))]
    (->
     (map #(% conf/io-feature->icon) ordered-script-features)
     reverse)))
