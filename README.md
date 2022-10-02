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

(sm/fdef-from-meta {:namespaces [my-cool-project.main]})
```
