(ns trabfinal-api.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [trabfinal-api.db :as db]
            [trabfinal-api.services :as services])
  (:gen-class))

;; ── Middleware de CORS ────────────────────────────────────────────────────────

(defn cors-headers [response]
  (-> response
      (assoc-in [:headers "Access-Control-Allow-Origin"] "*")
      (assoc-in [:headers "Access-Control-Allow-Headers"] "Content-Type, Accept")
      (assoc-in [:headers "Access-Control-Allow-Methods"] "GET, POST, OPTIONS")))

(defn wrap-cors [handler]
  (fn [request]
    (if (= :options (:request-method request))
      (cors-headers {:status 200 :body ""})
      (cors-headers (handler request)))))

(defroutes api-routes

  (GET "/" []
    {:status 200 :body {:status "ok" :mensagem "Calculadora de Calorias API"}})

  (POST "/api/usuario" {body :body}
    (services/salvar-usuario! body)
    {:status 201 :body body})

  (GET "/api/usuario" []
    (if-let [usuario (services/obter-usuario)]
      {:status 200 :body usuario}
      {:status 404 :body {:erro "Usuário não cadastrado"}}))

  (POST "/api/alimento" {body :body}
    (let [transacao (services/registrar-alimento! body)]
      {:status 201 :body transacao}))

  (POST "/api/exercicio" {body :body}
    (let [transacao (services/registrar-exercicio! body)]
      {:status 201 :body transacao}))

  (GET "/api/extrato" [inicio fim]
    {:status 200 :body (services/obter-extrato inicio fim)})

  (GET "/api/saldo" [inicio fim]
    {:status 200 :body {:saldo (services/obter-saldo inicio fim)}})

  (POST "/api/zerar" []
    (db/zerar!)
    {:status 200 :body {:mensagem "Dados zerados"}})

  (route/not-found {:status 404 :body {:erro "Rota não encontrada"}}))


(def app
  (-> api-routes
      (wrap-json-body {:keywords? true})
      wrap-json-response
      wrap-cors
      (wrap-defaults api-defaults)))

(defn init []
  (db/zerar!)
  (println "Dados zerados — comece com exemplos novos."))

(defn -main [& args]
  (init)
  (let [porta (Integer/parseInt (or (first args) "3000"))]
    (println (str "Servidor iniciando na porta " porta "..."))
    (run-jetty app {:port porta :join? true})))
