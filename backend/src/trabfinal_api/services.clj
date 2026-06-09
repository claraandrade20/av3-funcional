(ns trabfinal-api.services
  (:require [trabfinal-api.db :as db]
            [clj-http.client :as http]
            [cheshire.core :as json]))

(def ^:private api-ninjas-key "9HvymEeY7y8Hd4Lz98jRiZYEYfVKGprk5QYUfyQh")

(defn- api-key []
  api-ninjas-key)

(defn- numero? [v]
  (number? v))

(defn- calorias-do-item [item]
  (let [cal (:calories item)]
    (cond
      (numero? cal) (int cal)
      :else (let [carbs (or (:carbohydrates_total_g item) 0)
                  fat   (or (:fat_total_g item) 0)
                  prot  (if (numero? (:protein_g item)) (:protein_g item) 0)]
              (int (+ (* carbs 4) (* fat 9) (* prot 4)))))))

(defn- peso-para-libras [peso-kg]
  (* (or peso-kg 70) 2.20462))

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
          _        (println "[DEBUG] Buscando alimento:" query)
          key      (api-key)
          _        (println "[DEBUG] Chave API:" (if (empty? key) "VAZIA ❌" "PRESENTE ✓"))
          resposta (http/get "https://api.api-ninjas.com/v1/nutrition"
                             {:headers      {"X-Api-Key" key}
                              :query-params {"query" query}
                              :as           :json
                              :socket-timeout 3000
                              :conn-timeout 3000})
          status   (:status resposta)
          itens    (:body resposta)]
      (println "[DEBUG] Status da resposta:" status)
      (println "[DEBUG] Resposta da API:" itens)
      (if (= status 200)
        (if (seq itens)
          (let [calorias (calorias-do-item (first itens))]
            (println "[SUCCESS] Calorias encontradas:" calorias)
            calorias)
          (do
            (println "[AVISO] API retornou lista vazia para:" query)
            100))
        (do
          (println "[ERRO] API retornou status:" status)
          100)))
    (catch Exception e 
      (do
        (println "[ERRO] Falha ao buscar calorias de alimento:")
        (println "[ERRO]" (.getMessage e))
        (.printStackTrace e)
        100))))
(defn buscar-calorias-exercicio [atividade duracao peso]
  (let [peso-kg  (or (try (Double/parseDouble (str peso)) (catch Exception _ 70)) 70)
        peso-lbs (peso-para-libras peso-kg)]
    (try
      (let [_        (println "[DEBUG] Buscando exercício:" atividade "duração:" duracao "peso:" peso-kg "kg (" peso-lbs "lbs)")
            key      (api-key)
            resposta (http/get "https://api.api-ninjas.com/v1/caloriesburned"
                               {:headers      {"X-Api-Key" key}
                                :query-params {"activity"  atividade
                                               "weight"    peso-lbs
                                               "duration"  duracao}
                                :as           :json
                                :socket-timeout 3000
                                :conn-timeout 3000})
            status   (:status resposta)
            itens    (:body resposta)]
        (println "[DEBUG] Status da resposta exercício:" status)
        (println "[DEBUG] Resposta da API exercício:" itens)
        (if (= status 200)
          (if (seq itens)
            (let [calorias (int (:total_calories (first itens)))]
              (println "[SUCCESS] Calorias de exercício encontradas:" calorias)
              calorias)
            (do
              (println "[AVISO] API retornou lista vazia para exercício:" atividade)
              (* 5 duracao)))
          (do
            (println "[ERRO] API retornou status:" status)
            (* 5 duracao))))
      (catch Exception e 
        (do
          (println "[ERRO] Falha ao buscar calorias de exercício:")
          (println "[ERRO]" (.getMessage e))
          (.printStackTrace e)
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
