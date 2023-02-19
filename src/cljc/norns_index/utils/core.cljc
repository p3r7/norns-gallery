(ns norns-index.utils.core)



;; COLLECTION: GENERIC

(defn find-indexes [needle haystack]
  (keep-indexed #(when (= %2 needle) %1) haystack))

(defn take-n-distinct-rand
  "Like `rand-nth` but returns a set of N distinct elements from COLL."
  [n coll]
  (when (> n (count coll))
    (throw (ex-info "Tried to get more elements than what the collection contains." {:ex-type :unexpected-type})))

  (let [t (transient #{})]
    (while (< (count t) n)
      (conj! t (rand-nth coll)))
    (persistent! t)))

(defn dissoc-in
  "Like `dissoc` but allowing to pass a path like `assoc-in`."
  [m ks]
  (update-in m (butlast ks) dissoc (last ks)))



;; COLLECTIONS: PREDICATES

(defn- entry-member-of-map? [entry coll]
  (let [[k v] entry]
    (and (contains? coll k)
         (= v (get coll k)))))

(defn member?
  "Returns a truthy value if V is found in collection COLL."
  [v coll]

  (when-not (coll? coll)
    (throw (ex-info "Argument `coll` is not a collection" {:ex-type :unexpected-type})))

  (cond
    (set? coll) (coll v)                ; sets can be used as fn

    (map? coll)
    (cond
      (and (vector? v)
           (= 2 (count v)))
      (entry-member-of-map? v coll)

      (and (map? v)
           (= 1 (count v)))
      (entry-member-of-map? (first v) coll)

      :default (throw (ex-info "Argument `coll` is a map, expecting `v` to be a vector of size 2 or map os size 1"
                               {:ex-type :unexpected-type,
                                :v v :coll coll})))

    :default (some #{v} coll)))



;; COLLECTIONS: GENERIC SEQUENCE PROCESSING

(defn maintain
  "Apply sequence processing FX (e.g. `map` or `filter`) with entry FN on each element of COLL while keeping the original collection type.

  Taken from book \"Clojure the Essential Reference\"."
  ([fx f coll]
   (into (empty coll) (fx f coll)))
  ([xform coll]
   (into (empty coll) xform coll)))

(defn map-vals
  "Returns a lazy hashmap consisting of the result of applying f to
    the value of each set in hashmap.
    Function f should accept one single argument."
  [f m]
  (persistent!
   (reduce-kv (fn [m k v] (assoc! m k (f v)))
              (transient (empty m)) m)))

(defn map-keys
  "Returns a lazy hashmap consisting of the result of applying f to
  the key of each set in hashmap.
  Function f should accept one single argument."
  [f m]
  (persistent!
   (reduce-kv (fn [m k v] (assoc! m (f k) v))
              (transient (empty m)) m)))

(defn continuous-partition [c]
  (let [first-partition (partition 2 c)
        second-partition (partition-all 2 (next c))]
    (->> (interleave first-partition second-partition)
         (remove #(= 1 (count %))))))



;; COLLECTIONS: FILTERING

(defn keep-in-coll
  "Return new collection of same type as COLL with only elements satisfying PREDICATE."
  [coll predicate]
  (when (not (coll? coll))
    (throw (ex-info "Argument `coll` is not a collection"
                    {:ex-type :unexpected-type,
                     :coll coll})))
  (maintain filter predicate coll))

(defn keep-vals-in-coll
  "Return new collection of same type as COLL with only elements whose values satisfy PREDICATE."
  [coll predicate]
  (when (not (coll? coll))
    (throw (ex-info "Argument `coll` is not a collection"
                    {:ex-type :unexpected-type,
                     :coll coll})))
  (let [predicate (if (map? coll)
                    (comp predicate val)
                    predicate)]
    (maintain filter predicate coll)))

(defn remove-in-coll
  "Return new collection of same type as COLL with elements satisfying PREDICATE removed."
  [coll predicate]
  (keep-in-coll coll (complement predicate)))

(defn remove-vals-in-coll
  "Return new collection of same type as COLL with elements whose values satisfy PREDICATE removed."
  [coll predicate]
  (keep-vals-in-coll coll (complement predicate)))

(defn remove-nils [coll]
  (remove-vals-in-coll coll nil?))

(defn remove-falsy-zero-or-empty [coll]
  (remove-vals-in-coll coll #(or
                              (not (boolean %))
                              (= 0 %)
                              (and (coll? %)
                                   (empty? %)))))



;; MAPS: MERGING

(defn deep-merge
  "Recursively merges MAPS.
  Like `merge` but for nested maps.

  Gotten from http://dnaeon.github.io/recursively-merging-maps-in-clojure/

  See also https://cljdoc.org/d/clojure-deep-merge/clojure-deep-merge/0.0.1/api/deep.merge"
  [& maps]
  (letfn [(m [& xs]
            (if (some #(and (map? %) (not (record? %))) xs)
              (apply merge-with m xs)
              (last xs)))]
    (reduce m maps)))

(defn deep-merge-with
  "Recursively merges MAPS and and for deepest shared level use MERGE-FN.
   Like `merge-into` but for nested maps."
  [merge-fn & maps]
  (letfn [(m [& xs]
            (if (every? #(and (map? %) (not (record? %))) xs)
              (apply merge-with m xs)
              (apply merge-fn xs)))]
    (reduce m maps)))

(defn deep-merge-with-ungarded
  "Recursively merges MAPS and for deepest level use MERGE-FN.
  Contrarily to `causal-struct.utils.core/deep-merge-with`, doesn't test the level depth is shared amongst all MAPS.
  Like `merge-into` but for nested maps."
  [merge-fn & maps]
  (letfn [(m [& xs]
            (if (some #(and (map? %) (not (record? %))) xs)
              (apply merge-with m xs)
              (apply merge-fn xs)))]
    (reduce m maps)))



;; MAPS

(defn- flattened-map-key
  [prefix k]
  (cond
    (nil? prefix)
    [k]

    (vector? prefix)
    (conj prefix k)

    :default
    [prefix k]))

(defn- flatten-map-kvs
  ([map] (flatten-map-kvs map nil))
  ([map prefix]
   (reduce
    (fn [memo [k v]]
      (if (map? v)
        (concat memo (flatten-map-kvs v (flattened-map-key prefix k)))
        (conj memo [(flattened-map-key prefix k) v])))
    [] map)))

(defn flatten-map
  [m]
  (into {} (flatten-map-kvs m)))



;; ATOMS

(defn atom?
  "Returns true if O is an atom."
  [o]
  #?(:clj     (= (class o) clojure.lang.Atom)
     :cljs    (= (type o) cljs.core/Atom)
     :default (throw (ex-info "Platform not supported" {:ex-type :unexpected-platform}))))

(defn multi-merge!
  "Recursively merges collection of maps MERGES into ATOM."
  [atom & merges]
  (let [merged (apply deep-merge merges)]
    (swap! atom (fn [a merged]
                  (deep-merge a merged)) merged)))
