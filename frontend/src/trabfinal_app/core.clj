(ns trabfinal-app.core
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.response :refer [redirect]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [html5]]
            [clj-http.client :as http]
            [cheshire.core :as json])
  (:gen-class))

(def backend "http://localhost:3000")

;; ── Layout base ──────────────────────────────────────────────────────────────

(defn layout [titulo & conteudo]
  (html5
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
     [:title titulo]
     [:style "
       body { font-family: sans-serif; max-width: 700px; margin: 40px auto; padding: 0 20px; color: #222; }
       nav { margin-bottom: 20px; }
       nav a { margin-right: 16px; text-decoration: none; color: #4a90e2; }
       nav a:hover { text-decoration: underline; }
       h1 { border-bottom: 2px solid #eee; padding-bottom: 8px; }
       label { display: block; margin-top: 12px; font-weight: bold; font-size: 0.9em; }
       input { display: block; margin: 4px 0 0; padding: 8px; width: 100%; box-sizing: border-box; border: 1px solid #ccc; border-radius: 4px; }
       button { margin-top: 16px; padding: 10px 24px; background: #4a90e2; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 1em; }
       button:hover { background: #357abd; }
       .msg { color: green; padding: 8px; background: #efffef; border-radius: 4px; }
       .saldo { font-size: 2em; font-weight: bold; margin: 16px 0; }
       table { width: 100%; border-collapse: collapse; margin-top: 16px; }
       th, td { padding: 10px; border: 1px solid #ddd; text-align: left; }
       th { background: #f5f5f5; }
       tr:hover { background: #fafafa; }
     "]]
    [:body
     [:nav
      [:a {:href "/"} "Início"]
      [:a {:href "/usuario"} "Usuário"]
      [:a {:href "/alimento"} "Alimento"]
      [:a {:href "/exercicio"} "Exercício"]
      [:a {:href "/extrato"} "Extrato"]]
     [:h1 titulo]
     conteudo]))

;; ── Helpers ───────────────────────────────────────────────────────────────────

(defn hoje []
  (.toString (java.time.LocalDate/now)))

(defn get-json [url params]
  (try
    (:body (http/get url {:query-params params :as :json}))
    (catch Exception _ nil)))

(defn post-json [url payload]
  (try
    (http/post url {:body (json/generate-string payload) :content-type :json})
    (catch Exception _ nil)))

;; ── Páginas ───────────────────────────────────────────────────────────────────

(defn page-index []
  (let [usuario (get-json (str backend "/api/usuario") {})
        saldo   (:saldo (get-json (str backend "/api/saldo") {"inicio" (hoje) "fim" (hoje)}))]
    (layout "Calculadora de Calorias"
      (when usuario [:p "Olá, " [:strong (:nome usuario)] " · " (:peso usuario) " kg"])
      [:p "Saldo calórico de hoje (" (hoje) "):"]
      [:p {:class "saldo"} (if saldo (str saldo " kcal") "—")]
      [:p
       [:a {:href "/alimento"} "Registrar alimento"] " · "
       [:a {:href "/exercicio"} "Registrar exercício"] " · "
       [:a {:href "/extrato"} "Ver extrato"]])))

(defn page-usuario [msg]
  (let [u (get-json (str backend "/api/usuario") {})]
    (layout "Dados do Usuário"
      (when msg [:p {:class "msg"} msg])
      [:form {:method "post" :action "/usuario"}
       [:label "Nome"]
       [:input {:type "text" :name "nome" :value (or (:nome u) "") :required true}]
       [:label "Peso (kg)"]
       [:input {:type "number" :step "0.1" :name "peso" :value (or (:peso u) "") :required true}]
       [:button "Salvar"]])))

(defn page-alimento [msg]
  (layout "Registrar Alimento"
    (when msg [:p {:class "msg"} msg])
    [:form {:method "post" :action "/alimento"}
     [:label "Alimento"]
     [:input {:type "text" :name "nome" :placeholder "ex: frango" :required true}]
     [:label "Quantidade (g)"]
     [:input {:type "number" :name "quantidade" :placeholder "ex: 100" :min "1" :required true}]
     [:label "Data"]
     [:input {:type "date" :name "data" :value (hoje) :required true}]
     [:button "Registrar"]]))

(defn page-exercicio [msg]
  (layout "Registrar Exercício"
    (when msg [:p {:class "msg"} msg])
    [:form {:method "post" :action "/exercicio"}
     [:label "Exercício"]
     [:input {:type "text" :name "nome" :placeholder "ex: corrida" :required true}]
     [:label "Duração (min)"]
     [:input {:type "number" :name "duracao" :placeholder "ex: 30" :min "1" :required true}]
     [:label "Data"]
     [:input {:type "date" :name "data" :value (hoje) :required true}]
     [:button "Registrar"]]))

(defn page-extrato [inicio fim]
  (let [inicio  (or inicio (hoje))
        fim     (or fim (hoje))
        extrato (get-json (str backend "/api/extrato") {"inicio" inicio "fim" fim})
        saldo   (:saldo (get-json (str backend "/api/saldo") {"inicio" inicio "fim" fim}))]
    (layout "Extrato"
      [:form {:method "get" :action "/extrato"}
       [:label "De"]
       [:input {:type "date" :name "inicio" :value inicio}]
       [:label "Até"]
       [:input {:type "date" :name "fim" :value fim}]
       [:button "Filtrar"]]
      [:p "Saldo do período: " [:strong (str (or saldo 0) " kcal")]]
      (if (seq extrato)
        [:table
         [:thead [:tr [:th "Tipo"] [:th "Nome"] [:th "Data"] [:th "Calorias"]]]
         [:tbody
          (for [t extrato]
            [:tr
             [:td (name (keyword (:tipo t)))]
             [:td (:nome t)]
             [:td (:data t)]
             [:td (:calorias t)]])]]
        [:p "Nenhum registro no período."]))))

;; ── Rotas ─────────────────────────────────────────────────────────────────────

(defroutes app-routes
  (GET "/" [] (page-index))

  (GET  "/usuario" [msg] (page-usuario msg))
  (POST "/usuario" [nome peso]
    (post-json (str backend "/api/usuario")
               {:nome nome :peso (Double/parseDouble peso)})
    (redirect "/usuario?msg=Salvo+com+sucesso"))

  (GET  "/alimento" [msg] (page-alimento msg))
  (POST "/alimento" [nome quantidade data]
    (post-json (str backend "/api/alimento")
               {:nome nome :quantidade (Integer/parseInt quantidade) :data data})
    (redirect "/alimento?msg=Registrado+com+sucesso"))

  (GET  "/exercicio" [msg] (page-exercicio msg))
  (POST "/exercicio" [nome duracao data]
    (post-json (str backend "/api/exercicio")
               {:nome nome :duracao (Integer/parseInt duracao) :data data})
    (redirect "/exercicio?msg=Registrado+com+sucesso"))

  (GET "/extrato" [inicio fim] (page-extrato inicio fim))

  (route/not-found "Página não encontrada"))

(def app
  (wrap-defaults app-routes (assoc-in site-defaults [:security :anti-forgery] false)))

(defn -main [& _]
  (run-jetty app {:port 3001 :join? false})
  (println "Frontend rodando em http://localhost:3001"))
