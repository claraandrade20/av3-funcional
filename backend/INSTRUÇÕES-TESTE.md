# Instruções de Teste - Backend Calva + APIs Ninjas

## 📋 Resumo das Correções

1. ✅ **Chave API** agora usa variável de ambiente `API_NINJAS_KEY`
2. ✅ **Logs melhorados** mostram detalhes de erro e status HTTP
3. ✅ **Tratamento de erros** verifica se API respondeu corretamente (status 200)

---

## 🚀 Como Iniciar com Calva

### Opção 1: Jack-in (Recomendado)

1. Abra VS Code na pasta `backend`
2. Pressione `Ctrl+Alt+C` → `Ctrl+Alt+J` (ou F1 e procure "Calva: Start a Project REPL")
3. Escolha **Leiningen** quando perguntado
4. Aguarde até ver `clj꞉trabfinal-api.handler꞉> ` no terminal

### Opção 2: Conectar a REPL Existente

Se você já tem um REPL rodando:
1. Pressione `Ctrl+Alt+C` → `Ctrl+Alt+C` (ou F1 procure "Calva: Connect to a running REPL")
2. Escolha a porta (geralmente `:nREPL port`)

---

## 🔧 Configurar Variável de Ambiente

**No PowerShell (Windows):**

```powershell
$env:API_NINJAS_KEY = "9HvymEeY7y8Hd4Lz98jRiZYEYfVKGprk5"
```

**Verificar se está set:**

```powershell
$env:API_NINJAS_KEY
```

---

## 🧪 Testar no Calva REPL

### 1. Carregar o namespace

```clojure
(require '[trabfinal-api.services :as services])
(require '[trabfinal-api.db :as db])
```

### 2. Testarbusca de calorias de alimento

```clojure
; Teste simples
(services/buscar-calorias-alimento "banana" 100)

; Deve retornar um número (ex: 89)
; Verifique o console para logs: 
; [DEBUG] Buscando alimento: 100g Banana
; [DEBUG] Status da resposta: 200
; [SUCCESS] Calorias encontradas: 89
```

### 3. Testar busca de calorias de exercício

```clojure
; Teste com peso
(services/buscar-calorias-exercicio "running" 30 70)

; Deve retornar um número positivo
; Logs devem mostrar: [SUCCESS] Calorias de exercício encontradas: XXX
```

### 4. Testar fluxo completo

```clojure
; Salvar usuário
(services/salvar-usuario! {:altura 170 :peso 70 :idade 25 :sexo "M"})

; Registrar alimento
(services/registrar-alimento! {:nome "apple" :data "2026-06-08" :quantidade 150})

; Registrar exercício
(services/registrar-exercicio! {:nome "walking" :data "2026-06-08" :duracao 45})

; Ver todas as transações
(services/obter-extrato "2026-06-08" "2026-06-08")

; Ver saldo
(services/obter-saldo "2026-06-08" "2026-06-08")
```

---

## 🌐 Testar as Rotas HTTP

### Iniciar servidor (do REPL do Calva)

```clojure
; Executar no REPL:
(require '[trabfinal-api.handler :as handler])

; Iniciar servidor na porta 3000
(def server (handler/-main "3000"))
```

**Ou via terminal:**

```bash
cd backend
$env:API_NINJAS_KEY = "9HvymEeY7y8Hd4Lz98jRiZYEYfVKGprk5"
lein ring server-headless
```

### Testar endpoints com curl

```bash
# 1. Verificar se servidor está rodando
curl http://localhost:3000/

# 2. Cadastrar usuário
curl -X POST http://localhost:3000/api/usuario ^
  -H "Content-Type: application/json" ^
  -d "{\"altura\":170,\"peso\":70,\"idade\":25,\"sexo\":\"M\"}"

# 3. Registrar alimento (DEVE ter calorias!)
curl -X POST http://localhost:3000/api/alimento ^
  -H "Content-Type: application/json" ^
  -d "{\"nome\":\"banana\",\"data\":\"2026-06-08\",\"quantidade\":100}"

# Resposta esperada:
# {"tipo":"alimento","nome":"Banana","data":"2026-06-08","quantidade":100,"calorias":89}

# 4. Registrar exercício
curl -X POST http://localhost:3000/api/exercicio ^
  -H "Content-Type: application/json" ^
  -d "{\"nome\":\"running\",\"data\":\"2026-06-08\",\"duracao\":30}"

# 5. Ver extrato
curl "http://localhost:3000/api/extrato?inicio=2026-06-08&fim=2026-06-08"

# 6. Ver saldo
curl "http://localhost:3000/api/saldo?inicio=2026-06-08&fim=2026-06-08"
```

---

## 🐛 Debugging - O que observar nos logs

### Se voltar apenas 100 calorias:

```
[DEBUG] Chave API: VAZIA ❌          ← Problema: variável não configurada
[ERRO] Falha ao buscar calorias...   ← Ver mensagem de erro completa
```

**Solução:** Configure `$env:API_NINJAS_KEY` antes de rodar

### Se receber erro 401:

```
[ERRO] API retornou status: 401
```

**Solução:** Chave API inválida ou expirada - verifique em https://api-ninjas.com

### Se receber erro 429:

```
[ERRO] API retornou status: 429
```

**Solução:** Muitas requisições - limite da API atingido (60/mês com free tier)

### Se receber lista vazia:

```
[AVISO] API retornou lista vazia para: 150g frango grelhado
```

**Solução:** a API só reconhece nomes em **inglês**. Troque pelo termo correto:

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

Lista completa e tabela PT → EN: veja `README.md` e `DOCUMENTACAO.md`.

---

## 📋 Checklist de Verificação

- [ ] Variável `$env:API_NINJAS_KEY` está configurada?
- [ ] Calva está conectado (vendo `clj꞉trabfinal-api.handler꞉> `)?
- [ ] Logs mostram status 200 da API?
- [ ] Calorias retornam valores > 0 (não apenas 100)?
- [ ] Servidor responde em http://localhost:3000?
- [ ] Endpoints retornam JSON válido?

---

## 📞 Próximas etapas

Se ainda tiver problemas:

1. **Reinicie o Calva:** Pressione `Ctrl+Alt+C` → `Ctrl+Alt+Q` (quit) e depois `Ctrl+Alt+J` (novo jack-in)
2. **Limpe o cache:** Delete a pasta `target/` e rode `lein clean`
3. **Verifique a internet:** Teste em navegador: `https://api.api-ninjas.com/v1/nutrition?query=100g+banana`
4. **Veja os logs completos:** O terminal Calva deve mostrar `[DEBUG]`, `[ERROR]`, etc.
