# meta-spec

Place fn specs inside the fn's metadata as:

```clj
(ns my.project
  (:require [clojure.spec.alpha :as s]
            [meta-spec.core :as ms]))

(defn foo
  {::ms/spec {:args (s/cat :x int? :y int?)
              :ret int?}}
  [x y]
  (+ x y))

(ms/fdef-from-meta {:ns [my.project]})
```

... instead of:

```clj
(ns my.project
  (:require [clojure.spec.alpha :as s]))

(defn foo [x y]
  (+ x y))

(s/fdef foo
        :args (s/cat :x int? :y int?)
        :ret int?)
```

List all fns that do not (yet) have a spec associated with them:

```clj
(ns my.project
  (:require [clojure.spec.alpha :as s]
            [meta-spec.core :as ms]))

(defn foo
  {::ms/spec {:args (s/cat :x int? :y int?)
              :ret int?}}
  [x y]
  (+ x y))

(defn bar [x y]
  (/ x y))

(ms/fdef-from-meta {:ns [my.project]})

(comment
  (ms/fns-without-specs {:ns ['meta-spec.core]}) => (#'my.project/bar)
  )
```

This can be helpful if you strive to have a project where all fns have specs.

## API

```clj
(meta-spec.core/fdef-from-meta
  {
   ;; A seq of namespaces
   :ns [my.project]

   ;; The meta keyword used to find spec definitions. Defaults to :meta-spec.core/spec
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
   :fn [my.ns/foo another.ns/and-fn]
   }
```
