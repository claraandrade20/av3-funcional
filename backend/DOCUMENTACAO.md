# Documentação Técnica — Backend (trabfinal-api)

## Visão Geral

O backend é uma API REST em **Clojure** com **Ring** e **Compojure**. Responsabilidades:

1. Armazenar usuário e transações (calorias consumidas e gastas) em memória
2. Consultar a **API Ninjas** para valores calóricos
3. Expor endpoints JSON consumidos pelo frontend (`http://localhost:3001`)

---

## Estrutura de Arquivos

```
backend/
├── project.clj
├── DOCUMENTACAO.md
├── INSTRUÇÕES-TESTE.md
└── src/trabfinal_api/
    ├── db.clj          # Estado em memória (atoms)
    ├── services.clj    # Lógica de negócio + API Ninjas
    └── handler.clj     # Rotas HTTP + middleware
```

---

## Dependências (`project.clj`)

| Biblioteca | Versão | Uso |
|------------|--------|-----|
| `compojure` | 1.6.1 | Roteamento HTTP |
| `ring/ring-defaults` | 0.3.2 | Middlewares padrão |
| `ring/ring-json` | 0.5.0 | Parse/serialização JSON |
| `cheshire` | 5.10.0 | JSON |
| `clj-http` | 3.12.3 | Cliente HTTP (API Ninjas) |

---

## Camada de Dados — `db.clj`

```clojure
(def usuario    (atom nil))
(def transacoes (atom '()))
```

Persistência em **Atoms** — dados são perdidos ao reiniciar o servidor.

### Estrutura dos dados

**Usuário** (campos usados pelo frontend):

```clojure
{:nome "João Silva"
 :peso 70}    ; kg — usado no cálculo de exercícios
```

O endpoint `POST /api/usuario` aceita qualquer mapa JSON; o frontend envia `nome` e `peso`.

**Transação de alimento:**

```clojure
{:tipo       :alimento
 :nome       "banana"
 :data       "2026-06-09"
 :quantidade 100          ; gramas
 :calorias   89}          ; positivo = consumo
```

**Transação de exercício:**

```clojure
{:tipo     :exercicio
 :nome     "running"
 :data     "2026-06-09"
 :duracao  30             ; minutos
 :calorias -300}          ; negativo = gasto
```

---

## Camada de Serviços — `services.clj`

### Funções auxiliares

| Função | Descrição |
|--------|-----------|
| `api-key` | Retorna a chave da API Ninjas (definida em `api-ninjas-key`) |
| `numero?` | Predicado para valores numéricos |
| `calorias-do-item` | Extrai `:calories` do item ou estima com macros (plano gratuito) |
| `peso-para-libras` | Converte peso em kg para libras (`× 2,20462`) |

### Funções puras

| Função | Descrição |
|--------|-----------|
| `data-no-periodo?` | `true` se a data está entre `inicio` e `fim` (strings `YYYY-MM-DD`) |
| `filtrar-por-periodo` | `filter` sobre transações do período |
| `calcular-saldo` | `reduce +` sobre `:calorias` |

### API Ninjas

Base: `https://api.api-ninjas.com`. A chave fica em `services.clj`:

```clojure
(def ^:private api-ninjas-key "sua-chave-aqui")
```

| Função | Endpoint | Parâmetros | Fallback |
|--------|----------|------------|----------|
| `buscar-calorias-alimento` | `GET /v1/nutrition` | `query` = `"{qtd}g {nome}"` | `100` |
| `buscar-calorias-exercicio` | `GET /v1/caloriesburned` | `activity`, `weight` (lbs), `duration` (min) | `5 × duração` |

Todas as chamadas usam timeout de 3 s e logam status, resposta e erros no console.

#### Estimativa de calorias (plano gratuito)

Quando `calories` ou `protein_g` vêm bloqueados (`"Only available for premium subscribers."`), o backend calcula:

```
(carbs × 4) + (fat × 9) + (protein × 4)
```

Carnes e ovos podem parecer subestimados porque a proteína costuma vir bloqueada no free tier.

#### Nomes em inglês

A consulta de nutrição usa `{quantidade}g {nome}`. Termos em português retornam lista vazia → fallback `100`.

**Alimentos validados:** `banana`, `apple`, `rice`, `bread`, `egg`, `milk`, `chicken breast`, `salmon`, `beef`, `pasta`, `potato`, `broccoli`, `cheese`, `yogurt`, `orange`, `avocado`, `tuna`, `pizza`, `lettuce`, `tomato`, `oatmeal`, `peanut butter`, `strawberry`, `grape`, `carrot`, `beans`, `corn`, `shrimp`, `tofu`, `hamburger`

**Exercícios validados:** `running`, `walking`, `cycling`, `swimming`, `yoga`, `dancing`, `hiking`, `jumping rope`, `soccer`, `basketball`, `tennis`, `boxing`, `rowing`, `volleyball`, `golf`, `weight lifting`, `stairs`

| Evite | Use em vez disso |
|-------|------------------|
| `weightlifting` | `weight lifting` |
| `stair climbing` | `stairs` |
| `pilates`, `elliptical` | `yoga`, `cycling` |

### Funções com efeito (`!`)

| Função | Comportamento |
|--------|---------------|
| `salvar-usuario!` | `reset!` em `usuario` |
| `registrar-alimento!` | API → transação → `swap!` em `transacoes` |
| `registrar-exercicio!` | API → calorias negativas → `swap!` |
| `obter-extrato` | Filtra `@transacoes` por período |
| `obter-saldo` | Soma calorias do extrato |

---

## Camada de Rotas — `handler.clj`

### Endpoints

| Método | Rota | Body / Query | Resposta |
|--------|------|--------------|----------|
| `GET` | `/` | — | `{"status":"ok","mensagem":"Calculadora de Calorias API"}` |
| `POST` | `/api/usuario` | `{"nome":"João","peso":70}` | dados salvos (201) |
| `GET` | `/api/usuario` | — | usuário (200) ou 404 |
| `POST` | `/api/alimento` | `{"nome":"banana","data":"2026-06-09","quantidade":100}` | transação (201) |
| `POST` | `/api/exercicio` | `{"nome":"running","data":"2026-06-09","duracao":30}` | transação (201) |
| `GET` | `/api/extrato` | `?inicio=&fim=` | lista de transações |
| `GET` | `/api/saldo` | `?inicio=&fim=` | `{"saldo": -211}` |

### Middleware

```
Requisição
    ↓ wrap-defaults (api-defaults)
    ↓ wrap-cors          — Access-Control-Allow-Origin: *
    ↓ wrap-json-response
    ↓ wrap-json-body     — keywords no body
    ↓ rota
```

`wrap-cors` responde `OPTIONS` com 200 (preflight para o frontend).

### Inicialização

```clojure
(defn -main [& args]
  ;; porta padrão 3000
  (run-jetty app {:port porta :join? true}))
```

---

## Como Executar

```powershell
cd backend
lein ring server-headless
```

API em `http://localhost:3000`.

---

## Exemplos curl

```bash
curl -X POST http://localhost:3000/api/usuario \
  -H "Content-Type: application/json" \
  -d '{"nome":"João Silva","peso":70}'

curl -X POST http://localhost:3000/api/alimento \
  -H "Content-Type: application/json" \
  -d '{"nome":"banana","data":"2026-06-09","quantidade":100}'

curl -X POST http://localhost:3000/api/exercicio \
  -H "Content-Type: application/json" \
  -d '{"nome":"running","data":"2026-06-09","duracao":30}'

curl "http://localhost:3000/api/extrato?inicio=2026-06-09&fim=2026-06-09"
curl "http://localhost:3000/api/saldo?inicio=2026-06-09&fim=2026-06-09"
```

---

## Princípios Funcionais

| Princípio | Onde |
|-----------|------|
| Imutabilidade | Mapas imutáveis; `swap!`/`reset!` em atoms |
| Funções puras | `filtrar-por-periodo`, `calcular-saldo`, `data-no-periodo?` |
| Ordem superior | `filter`, `map`, `reduce` |
| Composição | Middlewares com `->` em `handler.clj` |
| Efeitos isolados | Funções com sufixo `!` |
| Sem loops imperativos | Sem `for`, `while`, `loop`, `doseq` |

---

## Limitações

- Dados só em memória
- Um único usuário por instância
- Sem autenticação
- Sem cache da API Ninjas
- Nomes de alimentos/exercícios em inglês
- Plano gratuito limita campos nutricionais e volume de requisições
