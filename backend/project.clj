(defproject trabfinal-api "0.1.0-SNAPSHOT"
  :description "Calculadora de Calorias - Backend API"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :main trabfinal-api.handler
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [compojure "1.6.1"]
                 [ring/ring-core "1.8.1"]
                 [ring/ring-jetty-adapter "1.8.1"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.5.0"]
                 [cheshire "5.10.0"]
                 [clj-http "3.12.3"]]
  :plugins [[lein-ring "0.12.5"]]
  :ring {:handler trabfinal-api.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.2"]]}})
