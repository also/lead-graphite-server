(ns lead.graphite.pickle)

(def MARK (int \())
(def LIST (int \l))
(def DICT (int \d))
(def STRING (int \S))
(def SETITEM (int \s))
(def APPEND (int \a))
(def STOP (int \.))
(def INT (int \I))
(def FLOAT (int \F))
(def NONE (int \N))
(def NEWLINE (int \newline))
(def TRUE "I01\n")
(def FALSE "I00\n")

(def ascii (java.nio.charset.Charset/forName "US-ASCII"))

(defn- write-string
  [out string]
  (doto out
    (.write STRING)
    ; FIXME this almost certainly doesn't line up with python's string escaping
    (.write (.getBytes (pr-str string) ascii))
    (.write NEWLINE)))

(defn- write-int
  [out i]
  (doto out
    (.write INT)
    (.write (.getBytes (str i)))
    (.write NEWLINE)))

(defn- write-float [out f]
  (doto out
    (.write FLOAT)
    (.write (.getBytes (str f)))
    (.write NEWLINE)))

(defn write-none [out] (.write out NONE) out)
(defn write-setitem [out] (.write out SETITEM) out)
(defn write-append [out] (.write out APPEND) out)
(defn write-stop [out] (.write out STOP) out)

(defn write-bool
  [out b]
  (.write out (.getBytes (if b TRUE FALSE))) out)

(defn write-dict-start
  [out]
  (doto out
    (.write MARK)
    (.write DICT)))

(defn write-list-start
  [out]
  (doto out
    (.write MARK)
    (.write LIST)))

(defn write-values
  [out values]
  (doseq [v values]
    (cond
      (integer? v) (write-int out v)
      (float? v) (write-float out v)
      :else (write-none out))
    (write-append out))
  out)

(defn write-series
  [out series]
  (-> out
      (write-dict-start)

      (write-string "name")
      (write-string (:name series))
      (write-setitem)

      (write-string "step")
      (write-int (:step series))
      (write-setitem)

      (write-string "start")
      (write-int (:start series))
      (write-setitem)

      (write-string "end")
      (write-int (:end series))
      (write-setitem)

      (write-string "values")
      (write-list-start)
      (write-values (:values series))
      (write-setitem)))

(defn write-serieses
  [output-stream serieses]
  (let [out (java.io.DataOutputStream. output-stream)]
    (write-list-start out)
    (doseq [series serieses]
      (write-series out series)
      (write-append out))
    (write-stop out)))

(defn write-result
  [out result]
  (-> out
      (write-dict-start)

      (write-string "metric_path")
      (write-string (:name result))
      (write-setitem)

      ; much graphite. wow. such consistency.
      (write-string "isLeaf")
      (write-bool (:is-leaf result))
      (write-setitem)))

(defn write-query-results
  [output-stream results]
  (let [out (java.io.DataOutputStream. output-stream)]
    (write-list-start out)
    (doseq [result results]
      (write-result out result)
      (write-append out))
    (write-stop out)))