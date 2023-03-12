(ns norns-index.prerender
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



;; CLI OPTS

(def in-resource-dir "resources/public")
(def html-string-app-container "<div id=\"app\"></div>")
(def html-string-app-js "<script src=\"js/compiled/app.js\"></script>")

(def modes ["dir" "file"])

(def cli-options
  [[nil "--mode MODE" "Mode, either dir or file"
    ;; :validate [#(not (nil? (get modes %))) "Invalid mode"]
    ]
   [nil "--source-dir SOURCE_DIR" "Source dir file for mode=dir"
    :default in-resource-dir
    :validate [#(fs/existsSync %) "File does not exist"]]
   [nil "--source-html SOURCE_HTML" "Source HTML file to populate"
    :validate [#(fs/existsSync %) "File does not exist"]]
   [nil "--replace-tag REPLACE_TAG" "Tag to replace in source HTML file"
    :default html-string-app-js]
   [nil "--dest-dir DEST_DIR" "Output dir where to have the site generated"
    :validate [#(not (nil? %)) "Mandatory"]
    ]])

(def mandatory-args [:mode :dest-dir])



;; MAIN

(defn -main [& args]
  ;; NB: node.js lacks `XMLHttpRequest` to do ajax calls, so we inject it using lib `xhr2`
  (set! js/XMLHttpRequest (nodejs/require "xhr2"))

  (let [parsed (parse-opts args cli-options)
        opts (:options parsed)
        errs (:errors parsed)
        mode (:mode opts)
        dest-dir (:dest-dir opts)
        src-dir (when (= mode "dir")
                  (:source-dir opts))
        index (:source-html opts)
        index (if (and (nil? index) (not (nil? src-dir)))
                (path/join src-dir "index.html")
                index)

        mandatory-args (if (= mode "file")
                         (cons :source-html mandatory-args )
                         mandatory-args)
        ]

    ;; (pprint parsed)

    ;; --------------------------------
    ;; args validation

    (when (not (nil? errs))
      (fatal (string/join "\n" errs)))

    (doall
     (map
      #(when (nil? (% opts))
         (fatal (str "Missing mandatory argument --" (name %))))
      mandatory-args))

    (log "Generating site")

    (dynamic-conf/on-script-lookup
     (fn [scripts]
       (swap! state/state assoc :script-list scripts)
       ;; (pprint (:script-list @state/state))

       (log "Retrieved script index")

       (mkdirs dest-dir)

       (when (= mode "dir")
         (fs/cpSync src-dir dest-dir
                    #js {:force true
                         :recursive true
                         :filter (fn [src _dst]
                                   (and
                                    (not (string/includes? src "js/compiled/"))
                                    (not (string/includes? src "index.html"))))}))

       (let [html-template (fs/readFileSync index #js {:encoding "utf8"})
             ;; html-all-scritps (server/render-to-static-markup [views/main-view-all])
             html-all-scritps (server/render-to-string [views/main-view-all])
             html-page-all-scritps (-> html-template
                                       (clojure.string/replace html-string-app-js "")
                                       (clojure.string/replace html-string-app-container html-all-scritps))]
         (write-file (path/join dest-dir "index.html") html-page-all-scritps))

       (log "Wrote site")
       (js/process.exit 0)))))

(set! *main-cli-fn* -main)
