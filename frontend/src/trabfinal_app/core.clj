(ns trabfinal-app.core
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.response :refer [redirect]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [html5]]
            [clj-http.client :as http]
            [cheshire.core :as json]
            [clojure.java.browse :as browse])
  (:gen-class))

(def backend "http://localhost:3000")


(defn layout [titulo & conteudo]
  (html5
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
     [:title titulo]
     [:link {:rel "preconnect" :href "https://fonts.googleapis.com"}]
     [:link {:rel "preconnect" :href "https://fonts.gstatic.com"}]
     [:link {:href "https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;600;700&display=swap" :rel "stylesheet"}]
     [:style "
       * { margin: 0; padding: 0; box-sizing: border-box; }
       body { 
         font-family: 'Poppins', sans-serif; 
         background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
         min-height: 100vh;
         padding: 20px;
         color: #333;
       }
       .container {
         max-width: 800px;
         margin: 0 auto;
         background: white;
         border-radius: 20px;
         box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
         overflow: hidden;
       }
       nav {
         background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
         padding: 0;
         display: flex;
         flex-wrap: wrap;
         border-bottom: 4px solid #667eea;
       }
       nav a {
         flex: 1;
         text-decoration: none;
         color: white;
         padding: 15px 10px;
         text-align: center;
         font-weight: 600;
         font-size: 0.9em;
         transition: all 0.3s ease;
         border-bottom: 3px solid transparent;
       }
       nav a:hover {
         background: rgba(255, 255, 255, 0.1);
         border-bottom-color: #ffd700;
       }
       .content {
         padding: 40px;
       }
       h1 { 
         color: #667eea;
         margin-bottom: 30px;
         font-size: 2.2em;
         text-align: center;
         position: relative;
         padding-bottom: 15px;
       }
       h1::after {
         content: '';
         position: absolute;
         bottom: 0;
         left: 50%;
         transform: translateX(-50%);
         width: 60px;
         height: 4px;
         background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
         border-radius: 2px;
       }
       label { 
         display: block; 
         margin-top: 18px; 
         margin-bottom: 8px;
         font-weight: 600;
         color: #555;
         font-size: 0.95em;
       }
       input, textarea {
         display: block;
         width: 100%;
         padding: 12px 16px;
         margin-bottom: 4px;
         border: 2px solid #e0e0e0;
         border-radius: 10px;
         font-family: 'Poppins', sans-serif;
         font-size: 1em;
         transition: all 0.3s ease;
       }
       input:focus, textarea:focus {
         outline: none;
         border-color: #667eea;
         box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
       }
       button { 
         margin-top: 24px;
         width: 100%;
         padding: 14px;
         background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
         color: white;
         border: none;
         border-radius: 10px;
         cursor: pointer;
         font-size: 1em;
         font-weight: 600;
         transition: all 0.3s ease;
         box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
       }
       button:hover { 
         transform: translateY(-2px);
         box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
       }
       button:active {
         transform: translateY(0);
       }
       .msg { 
         color: #2e7d32;
         padding: 14px 16px;
         background: linear-gradient(135deg, #c8e6c9 0%, #a5d6a7 100%);
         border-radius: 10px;
         border-left: 4px solid #2e7d32;
         margin-bottom: 20px;
         font-weight: 500;
       }
       .welcome-section {
         background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
         padding: 24px;
         border-radius: 15px;
         margin-bottom: 28px;
         text-align: center;
       }
       .welcome-section p {
         font-size: 1.1em;
         color: #333;
         margin: 8px 0;
       }
       .welcome-section strong {
         color: #667eea;
         font-size: 1.2em;
       }
       .saldo-card {
         background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
         color: white;
         padding: 28px;
         border-radius: 15px;
         text-align: center;
         margin: 24px 0;
         box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
       }
       .saldo-label {
         font-size: 0.95em;
         opacity: 0.9;
         margin-bottom: 10px;
       }
       .saldo { 
         font-size: 3em;
         font-weight: 700;
         margin: 10px 0;
         letter-spacing: 2px;
       }
       .quick-actions {
         display: grid;
         grid-template-columns: 1fr 1fr 1fr;
         gap: 12px;
         margin-top: 24px;
       }
       .quick-actions a {
         display: block;
         padding: 12px;
         background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
         color: white;
         text-decoration: none;
         border-radius: 10px;
         text-align: center;
         font-weight: 600;
         transition: all 0.3s ease;
         font-size: 0.9em;
       }
       .quick-actions a:hover {
         transform: translateY(-3px);
         box-shadow: 0 6px 15px rgba(102, 126, 234, 0.3);
       }
       table { 
         width: 100%;
         border-collapse: collapse;
         margin-top: 24px;
         overflow: hidden;
         border-radius: 10px;
       }
       th {
         background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
         color: white;
         padding: 14px;
         text-align: left;
         font-weight: 600;
       }
       td {
         padding: 12px 14px;
         border-bottom: 1px solid #e0e0e0;
       }
       tr:hover { 
         background: #f9f9f9;
         transition: all 0.2s ease;
       }
       tr:last-child td {
         border-bottom: none;
       }
       .form-group {
         margin-bottom: 20px;
       }
       .filter-section {
         background: #f5f7fa;
         padding: 20px;
         border-radius: 10px;
         margin-bottom: 24px;
       }
       .filter-section label {
         display: inline-block;
         width: 48%;
         margin-right: 2%;
         margin-bottom: 12px;
       }
       .filter-section input {
         display: inline-block;
         width: 48%;
         margin-bottom: 0;
         margin-right: 2%;
       }
       .empty-state {
         text-align: center;
         padding: 40px 20px;
         color: #999;
       }
       .empty-state p {
         font-size: 1.1em;
         margin: 10px 0;
       }
       @media (max-width: 600px) {
         .content { padding: 20px; }
         h1 { font-size: 1.8em; }
         .saldo { font-size: 2.2em; }
         .quick-actions { grid-template-columns: 1fr; }
         nav a { font-size: 0.8em; padding: 12px 8px; }
         .filter-section label,
         .filter-section input {
           display: block;
           width: 100%;
           margin-right: 0;
           margin-bottom: 12px;
         }
       }
     "]]
    [:body
     [:div {:class "container"}
      [:nav
       [:a {:href "/"} "Início"]
       [:a {:href "/usuario"} "Usuário"]
       [:a {:href "/alimento"} "Alimento"]
       [:a {:href "/exercicio"} "Exercício"]
       [:a {:href "/extrato"} "Extrato"]]
      [:div {:class "content"}
       [:h1 titulo]
       conteudo]]]))

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

(defn page-index []
  (let [usuario (get-json (str backend "/api/usuario") {})
        saldo   (:saldo (get-json (str backend "/api/saldo") {"inicio" (hoje) "fim" (hoje)}))]
    (layout "Calculadora de Calorias"
      [:div {:class "welcome-section"}
       (if usuario
         [:div
          [:p "Bem-vindo, " [:strong (:nome usuario)] "! 👋"]
          [:p "Seu peso: " [:strong (str (:peso usuario) " kg")]]]
         [:p "Configure seus dados para começar"])]
      [:div {:class "saldo-card"}
       [:div {:class "saldo-label"} "Saldo Calórico de Hoje (" (hoje) ")"]
       [:div {:class "saldo"} (if saldo (str saldo " kcal") "—")]]
      [:div {:class "quick-actions"}
       [:a {:href "/alimento"} "\nRegistrar Alimento"]
       [:a {:href "/exercicio"} "\nRegistrar Exercício"]
       [:a {:href "/extrato"} "\nVer Extrato"]])))


(defn page-usuario [msg]
  (let [u (get-json (str backend "/api/usuario") {})]
    (layout "Dados do Usuário"
      (when msg [:p {:class "msg"} "✓ " msg])
      [:form {:method "post" :action "/usuario"}
       [:div {:class "form-group"}
        [:label "Nome Completo"]
        [:input {:type "text" :name "nome" :value (or (:nome u) "") :placeholder "Digite seu nome" :required true}]]
       [:div {:class "form-group"}
        [:label "Peso (kg)"]
        [:input {:type "number" :step "0.1" :name "peso" :value (or (:peso u) "") :placeholder "ex: 75.5" :required true}]]
       [:button "Salvar Dados"]])))

(defn page-alimento [msg]
  (layout "Registrar Alimento"
    (when msg [:p {:class "msg"} "✓ " msg])
    [:form {:method "post" :action "/alimento"}
     [:div {:class "form-group"}
      [:label "Nome do Alimento"]
      [:input {:type "text" :name "nome" :placeholder "ex: frango grelhado, arroz, maçã" :required true}]]
     [:div {:class "form-group"}
      [:label "Quantidade (gramas)"]
      [:input {:type "number" :name "quantidade" :placeholder "ex: 100" :min "1" :required true}]]
     [:div {:class "form-group"}
      [:label "Data"]
      [:input {:type "date" :name "data" :value (hoje) :required true}]]
     [:button "Registrar Alimento"]]))


(defn page-exercicio [msg]
  (layout "Registrar Exercício"
    (when msg [:p {:class "msg"} "✓ " msg])
    [:form {:method "post" :action "/exercicio"}
     [:div {:class "form-group"}
      [:label "Tipo de Exercício"]
      [:input {:type "text" :name "nome" :placeholder "ex: corrida, musculação, yoga" :required true}]]
     [:div {:class "form-group"}
      [:label "Duração (minutos)"]
      [:input {:type "number" :name "duracao" :placeholder "ex: 30" :min "1" :required true}]]
     [:div {:class "form-group"}
      [:label "Data"]
      [:input {:type "date" :name "data" :value (hoje) :required true}]]
     [:button "Registrar Exercício"]]))

(defn page-extrato [inicio fim]
  (let [inicio  (or inicio (hoje))
        fim     (or fim (hoje))
        extrato (get-json (str backend "/api/extrato") {"inicio" inicio "fim" fim})
        saldo   (:saldo (get-json (str backend "/api/saldo") {"inicio" inicio "fim" fim}))]
    (layout "Extrato"
      [:div {:class "filter-section"}
       [:form {:method "get" :action "/extrato"}
        [:label "Data Inicial"]
        [:input {:type "date" :name "inicio" :value inicio}]
        [:label "Data Final"]
        [:input {:type "date" :name "fim" :value fim}]
        [:button "Filtrar"]]]
      [:div {:class "saldo-card"}
       [:div {:class "saldo-label"} "Saldo do Período"]
       [:div {:class "saldo"} (str (or saldo 0) " kcal")]]
      (if (seq extrato)
        [:table
         [:thead [:tr [:th "📋 Tipo"] [:th "📝 Nome"] [:th "📅 Data"] [:th "🔥 Calorias"]]]
         [:tbody
          (for [t extrato]
            [:tr
             [:td (name (keyword (:tipo t)))]
             [:td (:nome t)]
             [:td (:data t)]
             [:td [:strong (:calorias t)]]])]]
        [:div {:class "empty-state"}
         [:p "📭"]
         [:p "Nenhum registro encontrado no período selecionado."]]))))


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

(defn- abrir-navegador! [porta]
  (let [url (str "http://localhost:" porta "/")]
    (println (str "Abrindo " url " no navegador..."))
    (try
      (browse/browse-url url)
      (catch Exception _
        (println (str "Nao foi possivel abrir o navegador. Acesse manualmente: " url))))))

(defn -main [& args]
  (let [porta (Integer/parseInt (or (first args) "3001"))]
    (println (str "Frontend iniciando na porta " porta "..."))
    (future
      (Thread/sleep 800)
      (abrir-navegador! porta))
    (run-jetty app {:port porta :join? true})))
