(ns clj-spec-meta.main
  (:require [clojure.spec.alpha :as s]))

(defn- myfn
  {::spec {:args (s/cat :a int? :b boolean?)
           :ret string?}
   :doc "my function"}
  [a b]
  (str a " " b))

(defmacro fdef-from-meta [{:keys [namespaces functions meta-kw]}]
  (let [meta-kw# (or meta-kw ::spec)
        fn-vars# (->> (map ns-interns namespaces)
                      (mapcat vals)
                      (filter #(and (fn? (var-get %))
                                    (get (meta %) meta-kw#)))
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
  (s/get-spec 'clj-spec-meta.main/myfn)
  (fns-without-specs {:namespaces ['clj-spec-meta.main]})
  )
