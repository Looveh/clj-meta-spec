(ns clj-spec-meta.main
  (:require [clojure.spec.alpha :as s]))

(defn- myfn
  {::spec {:args (s/cat :a int? :b boolean?)
           :ret string?}
   :doc "my function"}
  [a b]
  (str a " " b))

(defmacro fdef-from-meta [{:keys [namespaces functions meta-kw exclude-fns ns-regex]}]
  (let [meta-kw# (or meta-kw ::spec)
        ns-matching-re (when ns-regex
                         (filter #(re-matches ns-regex (str (ns-name %)))
                                 (all-ns)))
        fn-vars# (->> (concat namespaces ns-matching-re)
                      (distinct)
                      (map ns-interns)
                      (mapcat vals)
                      (filter #(and (fn? (var-get %))
                                    (get (meta %) meta-kw#)
                                    (not (contains? exclude-fns %))))
                      (concat functions))]
    `(do
       ~@(map (fn [fn-var]
                `(s/fdef ~(symbol fn-var)
                   :args ~(get-in (meta fn-var) [meta-kw# :args])
                   :ret ~(get-in (meta fn-var) [meta-kw# :ret])
                   :fn ~(get-in (meta fn-var) [meta-kw# :fn])))
              fn-vars#))))

(defn fns-without-specs [{:keys [namespaces]}]
  (->> (map ns-interns namespaces)
       (mapcat vals)
       (filter #(and (fn? (var-get %))
                     (not (s/get-spec (symbol %)))))))

(comment
  (macroexpand (quote (fdef-from-meta {:namespaces [clj-spec-meta.main]})))
  (fdef-from-meta {:namespaces [clj-spec-meta.main]})
  (macroexpand (quote (fdef-from-meta {:ns-regex #".*clj-spec-meta.*"})))
  (fdef-from-meta {:ns-regex #".*clj-spec-meta.*"})
  (s/get-spec 'clj-spec-meta.main/myfn)
  (fns-without-specs {:namespaces ['clj-spec-meta.main]})
  )
