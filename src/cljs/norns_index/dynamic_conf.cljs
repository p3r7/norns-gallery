(ns norns-index.dynamic-conf
  (:require-macros
   [cljs.core.async.macros :as asyncm :refer (go go-loop)])
  (:require
   [taoensso.encore :as encore]
   [norns-index.utils.core :refer [member? maintain]]
   [cljs.core.async :as async :refer (<! >! put! chan)]
   [cljs-http.client :as http]

   [norns-index.state :refer [state]]
   [norns-index.conf :as conf]))


(declare keep-script-wiki-page
         page-map->script-map community-entries->script-map)



;; CORE

(defn parse-json-string [s]
  (js->clj (.parse js/JSON s) :keywordize-keys true))



;; API

(defn on-script-lookup [cb & {:keys [tag]}]
  (go
    (let [url "https://raw.githubusercontent.com/monome/norns-community/main/community.json"
          response (<! (http/get url {:with-credentials? false
                                      ;; :headers {"Accept" "application/json"}
                                      }))]
      (if (not= 200 (:status response))
        (js/console.error "failed to retrieve data")
        ;; NB: github won't set "application/json" Content-Type for raw URLs
        ;; so we're forced to parse manually
        (->> (:entries (parse-json-string (:body response)))
             ;; REVIEW: eventually filter out uncategorized scripts using a `keep` filter?
             ;; (keep <keep-fn>)
             community-entries->script-map
             cb)))))

(defn get-script-index! [& {:keys [tag]}]
  (on-script-lookup (fn [scripts] (swap! state assoc :script-list scripts))
                    :tag tag))



;; META-DATA NORMALISATION

(defn script-categories-from-tags [tags]
    (into
     []
     (filter #(member? % conf/script-categories-order) tags)))

(defn script-io-features-from-tags [tags]
  (let [kw-tags (map #(-> %
                          (clojure.string/replace #" " "_")
                          keyword) tags)
        feature-kws (set conf/script-io-features-order)]
    (set
     (filter feature-kws kw-tags))))

(defn community-entry->script-def [entry]
  (let [tags (:tags entry)
        categories (script-categories-from-tags tags)
        io-features (script-io-features-from-tags tags)]
    [(:project_name entry)
     {:types categories
      :description (:description entry)
      :features io-features
      ;; :author (clojure.string/split (:author entry) #" ")
      :author (:author entry)
      :path (:documentation_url entry)}]))

(defn community-entries->script-map [entries]
  (into {} (map community-entry->script-def entries)))
