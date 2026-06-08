(ns trabfinal-api.services
  (:require [trabfinal-api.db :as db]
            [clj-http.client :as http]
            [cheshire.core :as json]))

(def api-key "9HvymEeY7y8Hd4Lz98jRiZYEYfVKGprk5")

(defn data-no-periodo? [data inicio fim]
  (and (>= (compare data inicio) 0)
       (<= (compare data fim) 0)))

(defn filtrar-por-periodo [transacoes inicio fim]
  (filter #(data-no-periodo? (:data %) inicio fim) transacoes))

(defn calcular-saldo [transacoes]
  (reduce + 0 (map :calorias transacoes)))

(defn buscar-calorias-alimento [nome quantidade]
  (try
    (let [query    (str quantidade "g " nome)
          _        (println "[DEBUG] Buscando alimento:" query "com chave:" (if (empty? api-key) "VAZIA" "OK"))
          resposta (http/get "https://api.api-ninjas.com/v1/nutrition"
                             {:headers      {"X-Api-Key" api-key}
                              :query-params {"query" query}
                              :as           :json
                              :socket-timeout 3000
                              :conn-timeout 3000})
          itens    (:body resposta)]
      (println "[DEBUG] Resposta da API:" itens)
      (when (seq itens)
        (int (:calories (first itens)))))
    (catch Exception e 
      (do
        (println "[ERRO] Falha ao buscar calorias de alimento:" (.getMessage e))
        100))))
(defn buscar-calorias-exercicio [atividade duracao peso]
  (let [peso-num (or (try (Double/parseDouble (str peso)) (catch Exception _ 70)) 70)]
    (try
      (let [_        (println "[DEBUG] Buscando exercício:" atividade "duração:" duracao "peso:" peso-num)
            resposta (http/get "https://api.api-ninjas.com/v1/caloriesburned"
                               {:headers      {"X-Api-Key" api-key}
                                :query-params {"activity"  atividade
                                               "weight"    peso-num
                                               "duration"  duracao}
                                :as           :json
                                :socket-timeout 3000
                                :conn-timeout 3000})
            itens    (:body resposta)]
        (println "[DEBUG] Resposta da API exercício:" itens)
        (when (seq itens)
          (int (:total_calories (first itens)))))
      (catch Exception e 
        (do
          (println "[ERRO] Falha ao buscar calorias de exercício:" (.getMessage e))
          (* 5 duracao))))))

(defn salvar-usuario! [dados]
  (reset! db/usuario dados))

(defn obter-usuario []
  @db/usuario)

(defn registrar-alimento! [{:keys [nome data quantidade]}]
  (let [calorias  (or (buscar-calorias-alimento nome quantidade) 0)
        transacao {:tipo      :alimento
                   :nome      nome
                   :data      data
                   :quantidade quantidade
                   :calorias  calorias}]
    (swap! db/transacoes #(concat % (list transacao)))
    transacao))

(defn registrar-exercicio! [{:keys [nome data duracao]}]
  (let [peso     (:peso (obter-usuario))
        calorias (or (buscar-calorias-exercicio nome duracao peso) 0)
        transacao {:tipo     :exercicio
                   :nome     nome
                   :data     data
                   :duracao  duracao
                   :calorias (- calorias)}]
    (swap! db/transacoes #(concat % (list transacao)))
    transacao))

(defn obter-extrato [inicio fim]
  (filtrar-por-periodo @db/transacoes inicio fim))

(defn obter-saldo [inicio fim]
  (calcular-saldo (obter-extrato inicio fim)))
