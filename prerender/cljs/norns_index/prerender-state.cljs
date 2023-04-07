(ns norns-index.prerender-state
  (:require
   ;; core
   [cljs.nodejs :as nodejs]
   [clojure.string :as string]
   [clojure.tools.cli :refer [parse-opts]]
   [goog.events :as evt]
   [reagent.core :as r]
   [reagent.dom.server :as server]
   [reagent.debug :refer [log error]]

   ;; state
   [norns-index.dynamic-conf :as dynamic-conf]
   ;; [norns-index.app :as app]
   [norns-index.state :as state]
   [norns-index.views :as views]

   ;; js libs
   [path :as path]
   [md5-file :as md5-file]
   [fs :as fs]))



;; CORE - HTML

(defn danger [t s]
  [t {:dangerouslySetInnerHTML {:__html s}}])

(defn add-cache-buster [resource-path path]
  (let [h (md5-file/sync (path/join resource-path path))]
    (str path "?" (subs h 0 6))))



;; CORE - FS

(defn mkdirs [f]
  (doseq [d (reductions #(str %1 "/" %2)
                        (-> (path/normalize f)
                            (string/split #"/")))]
    (when-not (fs/existsSync d)
      (fs/mkdirSync d))))

(defn write-file [f content]
  (log "Write" f)
  (mkdirs (path/dirname f))
  (fs/writeFileSync f content))

(defn write-resources [dir {:keys [css-file css-infiles]}]
  (write-file (path/join dir css-file)
              (->> css-infiles
                   (map #(fs/readFileSync %))
                   (string/join "\n"))))



;; CORE - LOGS

(defn pprint [v]
  (log (.stringify js/JSON (clj->js v))))

(defn fatal [msg]
  ;; (error msg)
  (log msg)
  (js/process.exit 1))



;; MAIN

(defn -main [& _args]
  ;; NB: node.js lacks `XMLHttpRequest` to do ajax calls, so we inject it using lib `xhr2`
  (set! js/XMLHttpRequest (nodejs/require "xhr2"))

  (reset! views/is-static true)

  (log "Generating state")

  (dynamic-conf/on-script-lookup
   (fn [scripts]
     (swap! state/state assoc :script-list scripts)
     ;; (pprint (:script-list @state/state))

     (log "Retrieved script index")

     (write-file (path/join "src/cljs/norns_index" "init-state.cljs") (str "(ns norns-index.init-state)\n(def state " @state/state ")"))

     (log "Wrote state")

     (js/process.exit 0))))

(set! *main-cli-fn* -main)
