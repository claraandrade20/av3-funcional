# Documentação Técnica — Backend (trabfinal-api)

## Visão Geral

O backend é uma API REST escrita em **Clojure** usando **Ring** e **Compojure**. Ele é responsável por:

1. Armazenar dados do usuário e transações (calorias consumidas e gastas) em memória
2. Consultar APIs externas para obter os valores calóricos
3. Expor endpoints JSON consumidos pelo frontend

---

## Estrutura de Arquivos

```
backend/
├── project.clj                          # Dependências e configuração do projeto
└── src/
    └── trabfinal_api/
        ├── db.clj                       # Estado em memória (atoms)
        ├── services.clj                 # Lógica de negócio + chamadas às APIs externas
        └── handler.clj                  # Rotas HTTP + middleware
```

---

## Dependências (`project.clj`)

| Biblioteca           | Versão | Uso                                         |
| -------------------- | ------ | ------------------------------------------- |
| `compojure`          | 1.6.1  | Roteamento HTTP                             |
| `ring/ring-defaults` | 0.3.2  | Middlewares padrão do Ring                  |
| `ring/ring-json`     | 0.5.0  | Parse e serialização automática de JSON     |
| `cheshire`           | 5.10.0 | Serialização JSON                           |
| `clj-http`           | 3.12.3 | Cliente HTTP para chamadas às APIs externas |

---

## Camada de Dados — `db.clj`

```clojure
(def usuario  (atom nil))
(def transacoes (atom []))
```

Toda a persistência é feita em **Atoms** do Clojure — a forma idiomática de gerenciar estado mutável em programação funcional. Os dados são perdidos ao reiniciar o servidor (comportamento esperado conforme o enunciado).

### Estrutura dos dados

**Usuário:**

```clojure
{:altura 170   ; cm
 :peso   70    ; kg
 :idade  25
 :sexo   "M"}
```

**Transação de alimento:**

```clojure
{:tipo       :alimento
 :nome       "banana"
 :data       "2026-06-01"
 :quantidade 100          ; gramas
 :calorias   89}          ; valor positivo = caloria consumida
```

**Transação de exercício:**

```clojure
{:tipo     :exercicio
 :nome     "running"
 :data     "2026-06-01"
 :duracao  30             ; minutos
 :calorias -300}          ; valor negativo = caloria gasta
```

---

## Camada de Serviços — `services.clj`

### Funções puras (sem efeitos colaterais)

| Função                                      | Descrição                                                                                           |
| ------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| `data-no-periodo? data inicio fim`          | Retorna `true` se a data está entre inicio e fim (comparação lexicográfica de strings `YYYY-MM-DD`) |
| `filtrar-por-periodo transacoes inicio fim` | Usa `filter` para retornar só as transações do período                                              |
| `calcular-saldo transacoes`                 | Usa `reduce + map` para somar todas as calorias (alimentos positivos + exercícios negativos)        |

### Chamadas às APIs externas

A aplicação usa a **API Ninjas** (`api.api-ninjas.com`). A chave deve ser configurada na variável de ambiente `API_NINJAS_KEY`.

```bash

# Windows (PowerShell)
$env:API_NINJAS_KEY = "sua-chave-aqui"
```

| Função                                             | Endpoint chamado                                                | Retorno                           |
| -------------------------------------------------- | --------------------------------------------------------------- | --------------------------------- |
| `buscar-calorias-alimento nome qtd`                | `GET /v1/nutrition?query=100g banana`                           | calorias (int); fallback `100` se falhar |
| `buscar-calorias-exercicio atividade duracao peso` | `GET /v1/caloriesburned?activity=running&weight=154&duration=30` | calorias (int); fallback `5 × minutos` se falhar |

Se a chamada falhar (sem chave, sem internet, nome não reconhecido, erro de API), o backend usa valores padrão: **100** para alimento e **5 × duração** para exercício.

### Nomes em inglês aceitos pela API Ninjas

A consulta enviada ao endpoint de nutrição tem o formato `{quantidade}g {nome}` (ex.: `150g chicken breast`). O nome deve estar em **inglês** — termos em português quase sempre retornam `[]`.

#### Alimentos validados (testados com `100g {nome}`)

`banana`, `apple`, `rice`, `bread`, `egg`, `milk`, `chicken breast`, `salmon`, `beef`, `pasta`, `potato`, `broccoli`, `cheese`, `yogurt`, `orange`, `avocado`, `tuna`, `pizza`, `lettuce`, `tomato`, `oatmeal`, `peanut butter`, `strawberry`, `grape`, `carrot`, `beans`, `corn`, `shrimp`, `tofu`, `hamburger`

#### Exercícios validados (testados com 30 min, peso ~150 lbs)

`running`, `walking`, `cycling`, `swimming`, `yoga`, `dancing`, `hiking`, `jumping rope`, `soccer`, `basketball`, `tennis`, `boxing`, `rowing`, `volleyball`, `golf`, `weight lifting`, `stairs`

#### Tradução PT → EN (referência rápida)

| Português | Use na API |
| --------- | ---------- |
| frango | `chicken breast` |
| feijão | `beans` |
| corrida | `running` |
| caminhada | `walking` |
| musculação | `weight lifting` |
| escada | `stairs` |

#### Nomes que **não** funcionam — use a alternativa

| Evite | Motivo | Use em vez disso |
| ----- | ------ | ---------------- |
| `frango`, `corrida`, `feijao` | português | `chicken breast`, `running`, `beans` |
| `weightlifting` | grafia não reconhecida | `weight lifting` |
| `stair climbing` | não listado | `stairs` |
| `pilates`, `elliptical` | não retornam resultados | `yoga`, `cycling` |

#### Plano gratuito

No free tier, `calories` e `protein_g` vêm como `"Only available for premium subscribers."`. O backend calcula calorias com `(carboidratos × 4) + (gordura × 9) + (proteína × 4)` quando o campo numérico não está disponível — proteínas ficam de fora, subestimando carnes e ovos.

### Funções com efeito (modificam estado)

| Função                      | O que faz                                            |
| --------------------------- | ---------------------------------------------------- |
| `salvar-usuario! dados`     | `reset!` no atom `usuario`                           |
| `registrar-alimento! body`  | chama API → cria mapa → `swap! conj` em `transacoes` |
| `registrar-exercicio! body` | idem, mas calorias são negadas (gastas)              |
| `obter-extrato inicio fim`  | lê `@transacoes` e filtra por período                |
| `obter-saldo inicio fim`    | calcula soma das calorias filtradas                  |

---

## Camada de Rotas — `handler.clj`

### Endpoints

| Método | Rota             | Body / Query                                             | Resposta                                      |
| ------ | ---------------- | -------------------------------------------------------- | --------------------------------------------- |
| `GET`  | `/`              | —                                                        | `{"status":"ok"}`                             |
| `POST` | `/api/usuario`   | `{"altura":170,"peso":70,"idade":25,"sexo":"M"}`         | dados salvos (201)                            |
| `GET`  | `/api/usuario`   | —                                                        | dados do usuário (200) ou erro (404)          |
| `POST` | `/api/alimento`  | `{"nome":"banana","data":"2026-06-01","quantidade":100}` | transação criada com calorias (201)           |
| `POST` | `/api/exercicio` | `{"nome":"running","data":"2026-06-01","duracao":30}`    | transação criada com calorias negativas (201) |
| `GET`  | `/api/extrato`   | `?inicio=2026-06-01&fim=2026-06-30`                      | lista de transações do período                |
| `GET`  | `/api/saldo`     | `?inicio=2026-06-01&fim=2026-06-30`                      | `{"saldo": -211}`                             |

### Middleware (cadeia de transformação)

```
Requisição HTTP
      ↓
wrap-defaults (api-defaults)  — configurações básicas Ring
      ↓
wrap-cors                     — adiciona headers CORS (*) para o frontend
      ↓
wrap-json-response            — serializa resposta Clojure → JSON
      ↓
wrap-json-body                — deserializa body JSON → mapa Clojure com keywords
      ↓
Rota (handler)
```

O middleware `wrap-cors` também responde a requisições `OPTIONS` (preflight) com status 200, necessário para navegadores com CORS.

---

## Como Executar

```bash
# 1. Entrar na pasta do backend
cd backend

# 2. (Opcional) configurar a chave da API
$env:API_NINJAS_KEY = "sua-chave"

# 3. Iniciar o servidor
lein ring server

# Ou sem abrir o navegador
lein ring server-headless
```

A API ficará disponível em `http://localhost:3000`.

---

## Exemplos de Uso (curl)

```bash
# Cadastrar usuário
curl -X POST http://localhost:3000/api/usuario \
  -H "Content-Type: application/json" \
  -d '{"altura":170,"peso":70,"idade":25,"sexo":"M"}'

# Registrar alimento
curl -X POST http://localhost:3000/api/alimento \
  -H "Content-Type: application/json" \
  -d '{"nome":"banana","data":"2026-06-01","quantidade":100}'

# Registrar exercício
curl -X POST http://localhost:3000/api/exercicio \
  -H "Content-Type: application/json" \
  -d '{"nome":"running","data":"2026-06-01","duracao":30}'

# Consultar extrato
curl "http://localhost:3000/api/extrato?inicio=2026-06-01&fim=2026-06-30"

# Consultar saldo
curl "http://localhost:3000/api/saldo?inicio=2026-06-01&fim=2026-06-30"
```

---

## Princípios Funcionais Aplicados

| Princípio                     | Onde aparece                                                                   |
| ----------------------------- | ------------------------------------------------------------------------------ |
| **Imutabilidade**             | Mapas e vetores são imutáveis; `swap!`/`reset!` criam novos valores            |
| **Funções puras**             | `filtrar-por-periodo`, `calcular-saldo`, `data-no-periodo?` — sem side effects |
| **Funções de ordem superior** | `filter`, `map`, `reduce` em `services.clj`                                    |
| **Composição**                | Cadeia de middlewares com `->` (threading macro) em `handler.clj`              |
| **Separação de efeitos**      | Funções puras isoladas das funções com `!` (efeito colateral)                  |
| **Sem loops imperativos**     | Nenhum `for`, `while`, `loop`, `doseq` — apenas recursão via `reduce`/`filter` |
