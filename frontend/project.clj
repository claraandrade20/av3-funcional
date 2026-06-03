(defproject trabfinal-app "0.1.0-SNAPSHOT"
  :description "Calculadora de Calorias - Frontend"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [ring/ring-core "1.8.1"]
                 [ring/ring-jetty-adapter "1.8.1"]
                 [ring/ring-defaults "0.3.2"]
                 [compojure "1.6.1"]
                 [hiccup "1.0.5"]
                 [clj-http "3.12.3"]
                 [cheshire "5.10.0"]]
  :main trabfinal-app.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
