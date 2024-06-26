(ns build
  (:require [cemerick.pomegranate.aether :as aether]
            [clojure.java.shell :as shell]
            [clojure.string :as str]
            [clojure.tools.build.api :as b]))

(def lib-name "config")
(def group-name "com.cleancoders.c3kit")
(def basis (b/create-basis {:project "deps.edn"}))
(def src-dirs (:paths basis))
(def lib (symbol group-name lib-name))
(def version (str/trim (slurp "VERSION")))
(def class-dir "target/classes")
(def jar-file (format "target/%s-%s.jar" lib-name version))
(def deploy-config (delay {:coordinates       [lib version]
                           :jar-file          jar-file
                           :pom-file          (str/join "/" [class-dir "META-INF/maven" group-name lib-name "pom.xml"])
                           :repository        {"clojars" {:url      "https://clojars.org/repo"
                                                          :username (System/getenv "CLOJARS_USERNAME")
                                                          :password (System/getenv "CLOJARS_PASSWORD")}}
                           :transfer-listener :stdout}))

(defn clean [_]
  (println "cleaning")
  (b/delete {:path "target"}))

(def pom-template
  [[:licenses
    [:license
     [:name "MIT License"]
     [:url "https://github.com/cleancoders/c3kit-apron/blob/master/LICENSE"]]]])

(defn pom [_]
  (println "writing pom.xml")
  (b/write-pom {:basis     basis
                :class-dir class-dir
                :lib       lib
                :version   version
                :pom-data  pom-template}))

(defn jar [_]
  (clean nil)
  (pom nil)
  (println "building" jar-file)
  (b/copy-dir {:src-dirs   src-dirs
               :target-dir class-dir})
  (b/jar {:class-dir class-dir
          :jar-file  jar-file}))

(defn tag [_]
  (let [clean? (str/blank? (:out (shell/sh "git" "diff")))
        tags   (delay (->> (shell/sh "git" "tag") :out str/split-lines set))]
    (cond (not clean?) (do (println "ABORT: commit master before tagging") (System/exit 1))
          (contains? @tags version) (println "tag already exists")
          :else (do (println "pushing tag" version)
                    (shell/sh "git" "tag" version)
                    (shell/sh "git" "push" "--tags")))))

(defn install [_]
  (jar nil)
  (println "installing " (:coordinates @deploy-config))
  (aether/install @deploy-config))

(defn deploy [_]
  (tag nil)
  (jar nil)
  (aether/deploy @deploy-config))
