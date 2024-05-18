(ns meta-spec.core
  ;; TODO
  ;; * Add cljs support, allegedly works within macros? With cljs.analyzer.api/all-ns?
  (:require [clojure.spec.alpha :as s]))

(defn- myfn
  {:spec {:args (s/cat :a int? :b boolean?)
          :ret string?}
   :doc "my function"}
  [a b]
  (str a " " b))

(defn- find-fn-vars [{:keys [ns-regex exclude-fn exclude-ns] fn' :fn ns' :ns}]
  (let [ns-matching-re (when ns-regex
                         (filter #(re-matches ns-regex (str (ns-name %)))
                                 (all-ns)))
        fn-in-ns (->> (concat ns' ns-matching-re)
                      (filter #(if exclude-ns
                                 (not (re-matches exclude-ns (str (ns-name %))))
                                 true))
                      (map ns-interns)
                      (mapcat vals)
                      (filter #(fn? (var-get %))))]
    (->> fn-in-ns
         (filter #(not (contains? exclude-fn %)))
         (concat fn')
         (distinct))))

(defmacro register
  ;; TODO
  ;; * Add docs
  ;; * Add tests
  ;; * Add spec
  ;; * Reconsider fn/arg names
  [& [{:keys [meta-kw ns-regex exclude-fn exclude-ns reset-specs?]
       fn' :fn
       ns' :ns
       :or {meta-kw :spec
            ns' [*ns*]}}]]
  (let [fn-vars (find-fn-vars {:meta-kw meta-kw
                               :ns ns'
                               :ns-regex ns-regex
                               :fn fn'
                               :exclude-fn exclude-fn
                               :exclude-ns exclude-ns})
        spec-resets (map (fn [fn-var]
                           (when reset-specs?
                             `(s/def ~(symbol fn-var) nil)))
                         fn-vars)
        spec-defs (map (fn [fn-var]
                         (let [{args' :args ret' :ret fn' :fn} (get (meta fn-var) meta-kw)]
                           (when (or args' ret' fn')
                             `(s/fdef ~(symbol fn-var)
                                :args ~args'
                                :ret ~ret'
                                :fn ~fn'))))
                       fn-vars)
        statements (->> (concat spec-resets spec-defs)
                        (filter some?))]
    (cons 'do statements)))

(defn fns-without-specs [{:keys [namespaces]}]
  (->> (map ns-interns namespaces)
       (mapcat vals)
       (filter #(and (fn? (var-get %))
                     (not (s/get-spec (symbol %)))))))

(comment
  (macroexpand (quote (fdef-from-meta {:ns [meta-spec.core]})))
  (register {:ns [meta-spec.core]})
  (register)
  (macroexpand (quote (fdef-from-meta {:ns-regex #".*meta-spec.*"})))
  (macroexpand (quote (fdef-from-meta {:exclude-ns #".*main.*"
                                       :ns-regex #".*meta-spec.*"})))
  (register {:ns-regex #".*meta-spec.*"})
  (s/get-spec 'meta-spec.core/myfn)
  (fns-without-specs {:namespaces ['meta-spec.core]})
  (s/def meta-spec.core/myfn nil))
