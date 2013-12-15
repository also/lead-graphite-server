(ns lead.graphite.api
  (:require [lead.graphite.pickle :as pickle]
            [lead.functions :refer [run]]
            [lead.parser :refer [parse]]
            [lead.connector :as conn]
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

(defroutes handler
  (GET "/metrics/find/" [format query from until]
       (when (= "pickle" format)
         (let [results (conn/query @conn/*connector* query)]
           {:status 200
            :headers {"Content-Type" "application/python-pickle"}
            :body (write-response pickle/write-query-results results)})))

  (GET "/render/" [format pickle target from until]
       (when (or (= "pickle" format)
                 (= "true" pickle))
         (let [result (run (parse target) {:start (Integer/parseInt from) :end (Integer/parseInt until)})]
           {:status 200
            :headers {"Content-Type" "application/python-pickle"}
            :body (write-response pickle/write-serieses result)}))))
