(ns trabfinal-api.services
  (:require [trabfinal-api.db :as db]
            [clj-http.client :as http]
            [cheshire.core :as json]))

(def api-key (or (System/getenv "API_NINJAS_KEY") ""))

(def alimentos-calorias
  {"frango"         165
   "carne"          250
   "peixe"          200
   "ovo"            70
   "leite"          60
   "queijo"         400
   "pão"            80
   "arroz"          130
   "feijão"         80
   "macarrão"       130
   "batata"         77
   "tomate"         18
   "alface"         15
   "banana"         89
   "maçã"           52
   "laranja"        47
   "chocolate"      500
   "brócolis"       30
   "cenoura"        41
   "abacate"        160})

(def exercicios-calorias
  {"corrida"        15
   "caminhada"      5
   "natação"        11
   "ciclismo"       12
   "musculação"     8
   "yoga"           3
   "pilates"        5
   "futebol"        10
   "voleibol"       8
   "dança"          7
   "boxe"           16
   "salto"          10})

(defn data-no-periodo? [data inicio fim]
  (and (>= (compare data inicio) 0)
       (<= (compare data fim) 0)))

(defn filtrar-por-periodo [transacoes inicio fim]
  (filter #(data-no-periodo? (:data %) inicio fim) transacoes))

(defn calcular-saldo [transacoes]
  (reduce + 0 (map :calorias transacoes)))

(defn buscar-calorias-alimento [nome quantidade]
  (let [nome-lower (clojure.string/lower-case nome)]
    (if-let [cal-por-100g (alimentos-calorias nome-lower)]
      (int (* cal-por-100g (/ quantidade 100)))
      (try
        (let [query    (str quantidade "g " nome)
              resposta (http/get "https://api.api-ninjas.com/v1/nutrition"
                                 {:headers      {"X-Api-Key" api-key}
                                  :query-params {"query" query}
                                  :as           :json
                                  :socket-timeout 3000
                                  :conn-timeout 3000})
              itens    (:body resposta)]
          (when (seq itens)
            (int (:calories (first itens)))))
        (catch Exception e 100))))) 

(defn buscar-calorias-exercicio [atividade duracao peso]
  (let [atividade-lower (clojure.string/lower-case atividade)
        peso-num (or (try (Double/parseDouble (str peso)) (catch Exception _ 70)) 70)]
    (if-let [cal-por-min (exercicios-calorias atividade-lower)]
      (int (* cal-por-min duracao (/ peso-num 70)))
      (try
        (let [resposta (http/get "https://api.api-ninjas.com/v1/caloriesburned"
                                 {:headers      {"X-Api-Key" api-key}
                                  :query-params {"activity"  atividade
                                                 "weight"    peso-num
                                                 "duration"  duracao}
                                  :as           :json
                                  :socket-timeout 3000
                                  :conn-timeout 3000})
              itens    (:body resposta)]
          (when (seq itens)
            (int (:total_calories (first itens)))))
        (catch Exception e (* 5 duracao)))))) 

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
