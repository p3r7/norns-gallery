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


(declare keep-script-page
         page-map->script-map)



;; WIKI.JS API

(defn get-from-wiki-js [& {:keys [tag]}]
  (go
    (let [url "https://norns.community/graphql"
          filter (when tag
                   (str "(tags: \"" tag "\")"))
          q (str "{pages { list" filter " { path, tags, description } } }")
          response (<! (http/get url
                                 {:with-credentials? false
                                  ;; :headers {"Authorization" (str "Bearer " bearer-token)}
                                  :query-params {"query" q}}))]
      (if (not= 200 (:status response))
        (js/console.error "failed to retrieve data")
        (->> (get-in response [:body :data :pages :list])
             (keep keep-script-page)
             (into {})
             page-map->script-map
             (swap! state assoc :script-list))))))

(defn keep-script-page [page]
  (encore/when-let [path (:path page)
                    matches (re-matches #"^authors/(.*)/(.*)" path)
                    [_ author script-name] matches]
    [script-name (into page {:author author})]))



;; META-DATA NORMALISATION

(defn script-categories-from-wiki-js-tags [tags]
  (cond
    (some #(member? % tags) ["looper" "live sampler"])
    ["loopers + live samplers"]

    :default
    (into
     []
     (filter #(member? % conf/script-categories-order) tags))))

(defn script-io-features-from-wiki-js-tags [tags]
  (let [kw-tags (map #(-> %
                          (clojure.string/replace #" " "_")
                          keyword) tags)
        feature-kws (set conf/script-io-features-order)]
    (set
     (filter feature-kws kw-tags))))


(defn wiki-js-page-def->script-def [page-def]
  (let [description (:description page-def)
        tags (:tags page-def)
        categories (script-categories-from-wiki-js-tags tags)
        io-features (script-io-features-from-wiki-js-tags tags)]
    {:types categories
     :description description
     :features io-features
     :author (:author page-def)
     :path (:path page-def)}))

(defn page-map->script-map [page-map]
  (maintain
   map
   (fn [[script-name page-def]]
     [script-name (wiki-js-page-def->script-def page-def)])
   page-map))
