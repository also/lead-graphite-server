(ns lead.graphite.api
  (:require [lead.graphite.pickle :as pickle]
            [lead.functions :refer [run]]
            [lead.parser :refer [parse]]
            [lead.connector :as conn]
            [lead.api :as api]
            [lead.api]
            [compojure.core :refer [defroutes GET]]
            [clojure.tools.logging :refer [warn]]))

(defn write-response
  [f & args]
  (ring.util.io/piped-input-stream
    (fn [out]
      (try
        (apply f out args)
        (catch Exception e
          (warn e "Exception building response"))))))

(defn- series->graphite-format [series]
  {:target (:name series)
   :datapoints
    (map vector
         (:values series)
         (range (:start series) Double/POSITIVE_INFINITY (:step series)))})

(defn eval-targets [targets opts]
  (flatten (vals (lead.api/eval-targets targets opts))))

(defroutes handler
  (GET "/metrics/find/" [format query from until]
       (when (= "pickle" format)
         (let [results (conn/query @conn/*connector* query)]
           {:status 200
            :headers {"Content-Type" "application/python-pickle"}
            :body (write-response pickle/write-query-results results)})))

  (GET "/render/" [format pickle target & params]
       (cond
         (or (= "pickle" format)
             (= "true" pickle))
         (let [opts (api/parse-request params)
               targets (if (string? target) [target] target)
               result (eval-targets targets opts)]
           {:status  200
            :headers {"Content-Type" "application/python-pickle"}
            :body    (write-response pickle/write-serieses result)})

         (= "json" format)
         (let [opts (api/parse-request params)
               targets (if (string? target) [target] target)
               result (eval-targets targets opts)]
           {:status  200
            :body    (map series->graphite-format result)}))))
