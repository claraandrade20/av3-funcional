(defproject trabfinal-app "0.1.0-SNAPSHOT"
  :description "Calculadora de Calorias - Frontend"
  :dependencies [[org.clojure/clojure "1.12.2"]
                 [ring/ring-core "1.12.2"]
                 [ring/ring-jetty-adapter "1.12.2"]
                 [ring/ring-defaults "0.5.0"]
                 [compojure "1.7.1"]
                 [hiccup "1.0.5"]
                 [clj-http "3.13.0"]
                 [cheshire "5.13.0"]]
  :main ^:skip-aot trabfinal-app.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
