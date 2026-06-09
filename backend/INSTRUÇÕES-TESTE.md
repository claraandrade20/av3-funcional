# Instruções de Teste — Backend (Calva + curl)

Guia para validar a API localmente com o REPL do Calva ou com requisições HTTP.

## O que o backend faz hoje

1. **Chave API Ninjas** embutida em `src/trabfinal_api/services.clj` — não precisa configurar variável de ambiente para rodar.
2. **Logs de debug** mostram status HTTP, resposta da API e mensagens de erro.
3. **Fallbacks** — alimento retorna `100` kcal; exercício retorna `5 × minutos` quando a API falha ou não reconhece o nome.
4. **Peso em libras** — a API de exercícios recebe o peso do usuário convertido de kg para lbs (`peso × 2,20462`).
5. **Plano gratuito** — quando `calories` vem bloqueado, o backend estima com `(carboidratos × 4) + (gordura × 9) + (proteína × 4)`.

---

## Iniciar com Calva

### Opção 1: Jack-in (recomendado)

1. Abra o VS Code na pasta `backend`
2. `Ctrl+Alt+C` → `Ctrl+Alt+J` (ou F1 → "Calva: Start a Project REPL")
3. Escolha **Leiningen**
4. Aguarde o prompt `clj꞉trabfinal-api.handler꞉> ` no terminal

### Opção 2: Conectar a um REPL existente

1. `Ctrl+Alt+C` → `Ctrl+Alt+C` (ou F1 → "Calva: Connect to a running REPL")
2. Selecione a porta `:nREPL port`

---

## Testar no REPL do Calva

### 1. Carregar namespaces

```clojure
(require '[trabfinal-api.services :as services])
(require '[trabfinal-api.db :as db])
```

### 2. Busca de calorias de alimento

```clojure
(services/buscar-calorias-alimento "banana" 100)
;; Esperado: número (ex.: 89)
;; Logs:
;; [DEBUG] Buscando alimento: 100g banana
;; [DEBUG] Chave API: PRESENTE ✓
;; [DEBUG] Status da resposta: 200
;; [SUCCESS] Calorias encontradas: 89
```

### 3. Busca de calorias de exercício

```clojure
(services/buscar-calorias-exercicio "running" 30 70)
;; Esperado: número positivo (ex.: ~300)
;; O peso 70 kg é convertido para ~154 lbs na chamada à API
```

### 4. Fluxo completo

```clojure
;; Cadastrar usuário (nome e peso — usados pelo frontend)
(services/salvar-usuario! {:nome "João Silva" :peso 70})

;; Registrar alimento (nome em inglês!)
(services/registrar-alimento! {:nome "apple" :data "2026-06-09" :quantidade 150})

;; Registrar exercício (nome em inglês!)
(services/registrar-exercicio! {:nome "walking" :data "2026-06-09" :duracao 45})

;; Extrato do dia
(services/obter-extrato "2026-06-09" "2026-06-09")

;; Saldo do dia (alimentos positivos + exercícios negativos)
(services/obter-saldo "2026-06-09" "2026-06-09")
```

---

## Testar rotas HTTP

### Iniciar servidor pelo REPL

```clojure
(require '[trabfinal-api.handler :as handler])
(def server (handler/-main "3000"))
```

### Iniciar pelo terminal

```powershell
cd backend
lein ring server-headless
```

### Endpoints com curl (PowerShell / cmd)

```powershell
# 1. Health check
curl http://localhost:3000/

# 2. Cadastrar usuário
curl -X POST http://localhost:3000/api/usuario ^
  -H "Content-Type: application/json" ^
  -d "{\"nome\":\"João Silva\",\"peso\":70}"

# 3. Registrar alimento
curl -X POST http://localhost:3000/api/alimento ^
  -H "Content-Type: application/json" ^
  -d "{\"nome\":\"banana\",\"data\":\"2026-06-09\",\"quantidade\":100}"

# Resposta esperada (exemplo):
# {"tipo":"alimento","nome":"banana","data":"2026-06-09","quantidade":100,"calorias":89}

# 4. Registrar exercício
curl -X POST http://localhost:3000/api/exercicio ^
  -H "Content-Type: application/json" ^
  -d "{\"nome\":\"running\",\"data\":\"2026-06-09\",\"duracao\":30}"

# 5. Extrato
curl "http://localhost:3000/api/extrato?inicio=2026-06-09&fim=2026-06-09"

# 6. Saldo
curl "http://localhost:3000/api/saldo?inicio=2026-06-09&fim=2026-06-09"
```

---

## Debugging — o que observar nos logs

### Alimento sempre com 100 kcal

```
[AVISO] API retornou lista vazia para: 150g frango grelhado
```

**Causas:** nome em português, alimento não reconhecido ou limite da API.

**Solução:** use nomes em **inglês** (tabela abaixo).

### Erro 401

```
[ERRO] API retornou status: 401
```

**Solução:** chave inválida ou expirada — atualize `api-ninjas-key` em `services.clj` com uma chave de [api-ninjas.com](https://api-ninjas.com).

### Erro 429

```
[ERRO] API retornou status: 429
```

**Solução:** limite do plano gratuito (60 req/mês). Aguarde ou reduza os testes.

### Exercício com valor redondo (ex.: 150 para 30 min)

```
[AVISO] API retornou lista vazia para exercício: corrida
```

**Solução:** use `running`, `walking`, etc. O fallback é `5 × minutos`.

---

## Nomes em inglês (obrigatório na API Ninjas)

| ❌ Não use | ✅ Use na API |
|-----------|---------------|
| frango, frango grelhado | `chicken breast` |
| corrida | `running` |
| caminhada | `walking` |
| feijão | `beans` |
| maçã | `apple` |
| banana | `banana` |
| musculação | `weight lifting` |

**Alimentos que funcionam:** `banana`, `apple`, `rice`, `bread`, `egg`, `milk`, `chicken breast`, `salmon`, `beef`, `pasta`, `potato`, `broccoli`, `cheese`, `yogurt`, `orange`, `avocado`, `tuna`, `pizza`, `lettuce`, `tomato`, `oatmeal`, `peanut butter`, `strawberry`, `grape`, `carrot`, `beans`, `corn`, `shrimp`, `tofu`, `hamburger`

**Exercícios que funcionam:** `running`, `walking`, `cycling`, `swimming`, `yoga`, `dancing`, `hiking`, `jumping rope`, `soccer`, `basketball`, `tennis`, `boxing`, `rowing`, `volleyball`, `golf`, `weight lifting`, `stairs`

Tabelas completas PT → EN: [`README.md`](../README.md) e [`DOCUMENTACAO.md`](DOCUMENTACAO.md).

---

## Checklist de verificação

- [ ] Calva conectado (prompt `clj꞉trabfinal-api.handler꞉> `)?
- [ ] Logs mostram `[DEBUG] Chave API: PRESENTE ✓`?
- [ ] Status HTTP `200` nas chamadas à API Ninjas?
- [ ] Calorias de alimento diferentes de `100` (com nomes em inglês)?
- [ ] Servidor responde em `http://localhost:3000/`?
- [ ] Endpoints retornam JSON válido?

---

## Se ainda houver problemas

1. **Reinicie o Calva:** `Ctrl+Alt+C` → `Ctrl+Alt+Q`, depois `Ctrl+Alt+J`
2. **Limpe o build:** `lein clean` (remove a pasta `target/`)
3. **Teste a internet:** abra `https://api.api-ninjas.com/v1/nutrition?query=100g+banana` no navegador (com header `X-Api-Key`)
4. **Confira a chave:** valor em `services.clj` deve coincidir com a chave ativa na sua conta API Ninjas
