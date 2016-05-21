
(set-env!
 :asset-paths #{"assets"}
 :source-paths #{"cirru-src"}
 :resource-paths #{}

 :dev-dependencies '[]
 :dependencies '[[org.clojure/clojurescript "1.8.40"      :scope "test"]
                 [org.clojure/clojure       "1.8.0"       :scope "test"]
                 [adzerk/boot-cljs          "1.7.170-3"   :scope "test"]
                 [adzerk/boot-reload        "0.4.6"       :scope "test"]
                 [cirru/boot-cirru-sepal    "0.1.5"       :scope "test"]
                 [binaryage/devtools        "0.5.2"       :scope "test"]
                 [mrmcc3/boot-rev   "0.1.0-SNAPSHOT"  :scope "test"]
                 [mvc-works/hsl             "0.1.2"]
                 [mvc-works/respo           "0.1.18"]
                 [mvc-works/respo-client    "0.1.11"]
                 [hiccup                    "1.0.5"]]

  :repositories #(conj % ["clojars" {:url "https://clojars.org/repo/"}]))

(require '[adzerk.boot-cljs   :refer [cljs]]
         '[adzerk.boot-reload :refer [reload]]
         '[cirru-sepal.core   :refer [cirru-sepal transform-cirru]]
         '[mrmcc3.boot-rev    :refer [rev rev-path]]
         '[clojure.java.io    :as    io]
         '[hiccup.core :refer [html]])

(def +version+ "0.1.0")

(task-options!
  pom {:project     'mvc-works/boot-workflow
       :version     +version+
       :description "Workflow"
       :url         "https://github.com/mvc-works/boot-workflow"
       :scm         {:url "https://github.com/mvc-works/boot-workflow"}
       :license     {"MIT" "http://opensource.org/licenses/mit-license.php"}})

(deftask compile-cirru []
  (cirru-sepal :paths ["cirru-src"]))

(defn html-dsl [data fileset]
  [:html
   [:head
    [:title "Boot workflow"]
    [:link
     {:rel "icon", :type "image/png", :href "respo.png"}]
    [:style nil "body {margin: 0;}"]
    [:style nil "body * {box-sizing: border-box; }"]]
    [:script {:id "config"  :type "text/edn"} (pr-str data)]
   [:body [:div#app] [:script {:src
    (let [script-name "main.js"]
      (if (:build? data)
        (rev-path fileset script-name)
        script-name))}]]])

(deftask html-file
  "task to generate HTML file"
  [d data VAL edn "data piece for rendering"]
  (with-pre-wrap fileset
    (let [tmp (tmp-dir!)
          out (io/file tmp "index.html")]
      (empty-dir! tmp)
      (spit out (html (html-dsl data fileset)))
      (-> fileset
        (add-resource tmp)
        (commit!)))))

(deftask dev []
  (comp
    (html-file :data {:build? false})
    (watch)
    (transform-cirru)
    (reload :on-jsload 'boot-workflow.core/on-jsload)
    (cljs)
    (target)))

(deftask build-simple []
  (comp
    (transform-cirru)
    (cljs :optimizations :simple)
    (html-file :data {:build? false})
    (target)))

(deftask build-advanced []
  (comp
    (transform-cirru)
    (cljs :optimizations :advanced)
    (rev :files [#"^[\w\.]+\.js$"])
    (html-file :data {:build? true})
    (target)))

(deftask rsync []
  (fn [next-task]
    (fn [fileset]
      (sh "rsync" "-r" "target/" "tiye:repo/mvc-works/boot-workflow" "--exclude" "main.out" "--delete")
      (next-task fileset))))

(deftask send-tiye []
  (comp
    (build-simple)
    (rsync)))

(deftask build []
  (set-env! :resource-paths #{"src/"})
  (comp
    (compile-cirru)
    (pom)
    (jar)
    (install)
    (target)))

(deftask deploy []
  (comp
    (build)
    (push :repo "clojars" :gpg-sign (not (.endsWith +version+ "-SNAPSHOT")))))
