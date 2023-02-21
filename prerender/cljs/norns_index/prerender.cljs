(ns norns-index.prerender
  (:require
   ;; core
   [cljs.nodejs :as nodejs]
   [clojure.string :as string]
   [goog.events :as evt]
   [reagent.core :as r]
   [reagent.dom.server :as server]
   [reagent.debug :refer [log]]

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



;; MAIN

(def html-string-app-container "<div id=\"app\"></div>")
(def html-string-app-js "<script src=\"js/compiled/app.js\"></script>")

(def in-resource-dir "resources/public")

(defn -main [& args]
  (let [[outdir] args]

    ;; NB: node.js lacks `XMLHttpRequest` to do ajax calls, so we inject it using lib `xhr2`
    (set! js/XMLHttpRequest (nodejs/require "xhr2"))

    (log "Generating site")

    (dynamic-conf/on-script-lookup
     (fn [scripts]
       (swap! state/state assoc :script-list scripts)
       ;; (log (.stringify js/JSON (clj->js (:script-list @state/state))))

       (mkdirs outdir)
       (fs/cpSync in-resource-dir outdir
                  #js {:force true
                       :recursive true
                       :filter (fn [src _dst]
                                 (and
                                  (not (string/includes? src "js/compiled/"))
                                  (not (string/includes? src "index.html"))))})

       (let [html-template (fs/readFileSync (path/join in-resource-dir "index.html") #js {:encoding "utf8"})
             ;; html-all-scritps (server/render-to-static-markup [views/main-view-all])
             html-all-scritps (server/render-to-string [views/main-view-all])
             html-page-all-scritps (-> html-template
                                       (clojure.string/replace html-string-app-js "")
                                       (clojure.string/replace html-string-app-container html-all-scritps))]
         (write-file (path/join outdir "index.html") html-page-all-scritps))

       (log "Wrote site")
       (js/process.exit 0)))))

(set! *main-cli-fn* -main)
