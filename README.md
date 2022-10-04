# clj-spec-main

Replace this:

```clj
(ns my-cool-project.main
  (:require [clojure.spec.alpha :as s]))

(defn my-fn [x y]
  (+ x y))

(s/fdef my-fn
        :args (s/cat :x int? :y int?)
        :ret int?)
```

... with this:

```clj
(ns my-cool-project.main
  (:require [clojure.spec.alpha :as s]
            [clj-spec-meta.main :as sm]))

(defn my-fn
  {::sm/spec {:args (s/cat :x int? :y int?)
              :ret int?}}
  [x y]
  (+ x y))

(sm/fdef-from-meta {:ns [my-cool-project.main]})
```

Help yourself remember to add specs to all of your functions, easily:

```clj
(ns my-cool-project.main
  (:require [clojure.spec.alpha :as s]
            [clj-spec-meta.main :as sm]))

(defn my-fn
  {::sm/spec {:args (s/cat :x int? :y int?)
              :ret int?}}
  [x y]
  (+ x y))

(defn my-other-fn [x y]
  (/ x y))

(sm/fdef-from-meta {:ns [my-cool-project.main]})

(comment
  (sm/fns-without-specs {:ns ['clj-spec-meta.main]}) => (#'my-cool-project.main/my-other-fn)
  )
```

## API

```clj
(clj-spec-meta.main/fdef-from-meta
  {
   ;; A seq of namespaces
   :ns [my-cool-project.main]

   ;; The meta keyword used to find spec definitions. Defaults to :clj-spec-meta.main/spec
   :meta-kw :my.ns/spec

   ;; Finds all namespaces that matches this regex
   :ns-regex #".*my\.lib\..*"

   ;; Exclude some of the found namespaces
   :exclude-ns [my.lib.excluded]

   ;; Exclude some fns from all found namespaces
   :exclude-fn ['my.ns/excluded-fn]

   ;; Reset specs of fns that don't contain meta-kw specs. Useful when removing
   ;; specs as code gets reloaded during development. Note that this may remove
   ;; specs defined with (s/fdef ...), use with care.
   :reset-specs? true

   ;; A seq of fn symbols
   :fn [my.ns/my-fn another.ns/and-fn]
   }
```
