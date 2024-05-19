# meta-spec

`meta-spec` allows you to flace fn spec definitions inside the fn's metadata as:

```clj
(defn foo
  {:spec {:args (s/cat :x int? :y int?)
          :ret int?}}
  [x y]
  (+ x y))
```

... instead of defining them with separate `fdef` statements as:

```clj
(defn foo [x y]
  (+ x y))

(s/fdef foo
        :args (s/cat :x int? :y int?)
        :ret int?)
```

`meta-spec` does not extend `clojure.spec.alpha` in any way, it is simply a
helper API that lets you place specs closer to the thing they're spec'ing and
treat specs as any other kind of metadata associated with your fns.

## Clojure and ClojureScript support

`meta-spec` currently only supports Clojure on the JVM and does not support
ClojureScript. It is the intention of the author to implement ClojureScript
support at some point.

## Usage examples

Calling `(ms/register)` without any arguments will search through the current ns
and register all found specs with `s/fdef`.

```clj
(ns my.project.core
  (:require [clojure.spec.alpha :as s]
            [meta-spec.core :as ms]))

(defn foo
  {:spec {:args (s/cat :x int? :y int?)
          :ret int?}}
  [x y]
  (+ x y))

(ms/register) ; registers specs for my.project.core/foo

(ns my.project.other
  (:require [clojure.spec.alpha :as s]
            [meta-spec.core :as ms]))

(defn bar
  {:spec {:args (s/cat :x str? :y str?)
          :ret str?}}
  [x y]
  (str x y))

(ms/register) ; registers specs for my.project.other/bar

(ns my.project.other)

(defn baz [x y]
  (- x y))

(ms/register) ; does nothing in my.project.other
```

Since calling `(ms/register)` in each ns is tedious and easily forgotten you can
trigger registering your specs in a single place in your project with the
`:ns-regex` argument. This must be called after all of your fns have been
defined at run time.

```clj
(ns my.project.core
  (:require [clojure.spec.alpha :as s]))

(defn foo
  {:spec ...
  [x y]
  ...)

(ns my.project.other
  (:require [clojure.spec.alpha :as s]))

(defn bar
  {:spec ...
  [x y]
  ...)

(ns my.project.setup
  (:require [meta-spec.core :as ms]))

;; register specs for all fns in ns matching my.project.*
(ms/register
  :ns-regex [#".*my\.project\..*"])
```

`meta-spec` provides the helper fn `fns-without-specs` that returns a list of
symbols of fns that do not have any specs. This is intended to be used to assess
spec coverage.

```clj
(ns my.project.core
  (:require [clojure.spec.alpha :as s]
            [meta-spec.core :as ms]))

(defn foo
  {:spec {:args (s/cat :x int? :y int?)
          :ret int?}}
  [x y]
  (+ x y))

(defn bar [x y]
  (/ x y))

(ms/register)

(ns my.project.coverage
  (:require [meta-spec.core :as ms]))

(comment
  (ms/fns-without-specs
    {:ns-regex [#".*my\.project\..*"]}) ; => (#'my.project.core/bar)
  )
```

## API

```clj
(meta-spec.core/register
  {
   ;; A seq of regexes. meta-spec will sequentially search for fn definitions in
   ;; all namespaces that matches these regexes. Note that the namespaces must
   ;; already have been loaded since specs are registered at run time.
   :ns-regex [#".*my\.lib\..*" #".*another\.project\..*"]

   ;; A keyword. This is the selector that meta-spec will use to extract spec
   ;; definitions from fn metadata. Defaults to :spec if the argument is not
   ;; provided.
   :meta-kw :my.ns/spec

   ;; A seq of namespaces. meta-spec will search for fn definitions in these
   ;; namespaces and register all specs found in their metadata. Defaults to the
   ;; current namespace [*ns*] if the argument not provided.
   :ns [my.project]

   ;; A seq of fn symbols. meta-spec will register specs for these fns.
   :fn [my.ns/foo another.ns/and-fn]

   ;; A seq of namespaces. meta-spec will not register specs for fns found in
   ;; these namespaces.
   :exclude-ns [my.lib.excluded]

   ;; A seq of symbols. meta-spec will not register specs for these fns.
   :exclude-fn ['my.ns/excluded-fn]

   ;; When an fn spec is already found in clojure.spec.alpha's global registry
   ;; meta-spec will first remove it from the registry before registering the
   ;; new spec. This is useful when hot reloading code during development. Use
   ;; with care since this may (s/fdef ...). Defaults to true.
   :reset-specs? true
   })

(meta-spec.core/fns-without-specs
  ;; TODO
  )
```

## Rationale

As projects grow in size or age it's natural that its authors can't hold all of
the code's context in their heads. `spec` not only a a great tool for run time
validation and instrumentation but also for documenting shapes of values and
intentions of fns. The author can attest to having worked on at least one
project large enough that they wouldn't have been able to be net productive with
their limited mental capacity without heavy reliance on `spec`.

`meta-spec` arose from the want to bring fn spec definitions "closer to home" by
placing them _right there_ in your fn defnitions as `(defn my-fn {:spec ...} []
...)`, rather than someplace else with a detached `(s/fdef my-fn ...)`
statement.


It's the belief of the author that a) a code base with fn specs will by and
large be easier to grok than one without, b) inlined specs are easier to grok
than detached specs and that c) writing inlined specs is more ergonomic and easy
to remember than writing detached ones, leading to greater spec coverage.
