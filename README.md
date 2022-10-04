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
