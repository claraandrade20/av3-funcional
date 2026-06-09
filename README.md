# Calculadora de Calorias (Clojure REST API + Web)

Projeto acadêmico desenvolvido em **Clojure** para a disciplina de **Programação Funcional (T300)** na Universidade de Fortaleza. O sistema monitora consumo e gasto calórico através de um backend REST e interface web responsiva, consultando a **API Ninjas** para obter valores nutricionais.

```
trabfinal-api/
├── backend/                    # Servidor REST (API)
│   ├── src/trabfinal_api/
│   │   ├── handler.clj         # Rotas HTTP
│   │   ├── services.clj        # Lógica de negócio + API Ninjas
│   │   └── db.clj              # Gerenciamento de dados (Atoms)
│   ├── test/
│   ├── project.clj
│   ├── .env.example            # Modelo da chave da API
│   ├── start-backend.ps1       # Script de inicialização (Windows)
│   ├── DOCUMENTACAO.md         # Documentação técnica do backend
│   └── INSTRUÇÕES-TESTE.md     # Guia de testes com Calva/curl
│
├── frontend/                   # Aplicação Web
│   ├── src/trabfinal_app/
│   │   └── core.clj            # Interface com usuário (Hiccup)
│   ├── test/
│   └── project.clj
│
└── README.md
```

## Pré-requisitos

| Software | Versão | Observação |
|----------|--------|------------|
| Java JDK | 8+ | Runtime do Clojure |
| Leiningen | 2.0+ | Gerenciador de dependências |
| Conta API Ninjas | — | Chave gratuita em [api-ninjas.com](https://api-ninjas.com) |

```bash
# Verificar instalações
java -version
lein version
```

## Configuração da API Ninjas

O backend precisa da variável de ambiente `API_NINJAS_KEY` para buscar calorias reais. Sem ela, ou quando a API falha, o sistema usa valores padrão (**100 kcal** para alimento, **5 × minutos** para exercício).

**Opção 1 — arquivo `.env` (recomendado no Windows)**

```powershell
cd backend
copy .env.example .env
# Edite .env e coloque sua chave em API_NINJAS_KEY=
```

**Opção 2 — variável manual no PowerShell**

```powershell
$env:API_NINJAS_KEY = "sua-chave-aqui"
```

> O arquivo `.env` não vai para o Git (está no `.gitignore`). Nunca commite a chave.

## Como Executar

A arquitetura exige **dois terminais simultâneos**:

### Terminal 1 — Backend (API)

**Windows (com `.env` configurado):**

```powershell
cd backend
.\start-backend.ps1
```

**Ou manualmente:**

```powershell
cd backend
$env:API_NINJAS_KEY = "sua-chave-aqui"
lein ring server-headless
```

*Use `lein ring server` se quiser que o Ring abra o navegador automaticamente.*

**Resultado esperado:** API em `http://localhost:3000`

### Terminal 2 — Frontend (interface web)

```powershell
cd frontend
lein run
```

**Resultado esperado:** aplicação em `http://localhost:3001`

## Funcionalidades

| Recurso | Descrição |
|---------|-----------|
| **Cadastro de Usuário** | Nome e peso (usado no cálculo de exercícios) |
| **Registro de Alimentos** | Alimento, quantidade (g), data — calorias via API Ninjas |
| **Registro de Exercícios** | Tipo, duração (min), data — gasto calórico via API Ninjas |
| **Extrato** | Lista transações em um período |
| **Saldo** | Balanço calórico (consumo − gasto) |

## Principais Endpoints

```
POST   /api/usuario              # Registrar usuário
GET    /api/usuario              # Consultar usuário
POST   /api/alimento             # Registrar alimento
POST   /api/exercicio            # Registrar exercício
GET    /api/extrato              # Listar transações (?inicio=&fim=)
GET    /api/saldo                # Calcular balanço (?inicio=&fim=)
```

## Modelo de Dados

```clojure
;; Alimento — nome em inglês para a API Ninjas reconhecer
{:tipo :alimento
 :nome "chicken breast"
 :quantidade 150
 :data "2026-06-05"
 :calorias 165}

;; Exercício — nome em inglês
{:tipo :exercicio
 :nome "running"
 :duracao 30
 :data "2026-06-05"
 :calorias -300}

;; Usuário
{:nome "João Silva"
 :peso 75.5}
```

## API Ninjas — nomes em inglês (obrigatório)

A API reconhece termos em **inglês**. Nomes em português (`frango`, `corrida`, `feijão`) costumam retornar lista vazia e o sistema grava o valor padrão — **isso não é o valor real**.

Cadastre no formulário exatamente como na coluna **Use na API**:

### Alimentos

| Português (não use) | Use na API |
|---------------------|------------|
| banana | `banana` |
| maçã | `apple` |
| arroz | `rice` |
| pão | `bread` |
| ovo | `egg` |
| leite | `milk` |
| frango | `chicken breast` |
| salmão | `salmon` |
| carne bovina | `beef` |
| macarrão | `pasta` |
| batata | `potato` |
| brócolis | `broccoli` |
| queijo | `cheese` |
| iogurte | `yogurt` |
| laranja | `orange` |
| abacate | `avocado` |
| atum | `tuna` |
| pizza | `pizza` |
| alface | `lettuce` |
| tomate | `tomato` |
| aveia | `oatmeal` |
| pasta de amendoim | `peanut butter` |
| morango | `strawberry` |
| uva | `grape` |
| cenoura | `carrot` |
| feijão | `beans` |
| milho | `corn` |
| camarão | `shrimp` |
| tofu | `tofu` |
| hambúrguer | `hamburger` |

### Exercícios

| Português (não use) | Use na API |
|---------------------|------------|
| corrida | `running` |
| caminhada | `walking` |
| ciclismo | `cycling` |
| natação | `swimming` |
| yoga | `yoga` |
| dança | `dancing` |
| trilha | `hiking` |
| pular corda | `jumping rope` |
| futebol | `soccer` |
| basquete | `basketball` |
| tênis | `tennis` |
| boxe | `boxing` |
| remo | `rowing` |
| vôlei | `volleyball` |
| golfe | `golf` |
| musculação | `weight lifting` |
| escada | `stairs` |

### Nomes que não funcionam — use a alternativa

| Evite | Use em vez disso |
|-------|------------------|
| `weightlifting` | `weight lifting` |
| `stair climbing` | `stairs` |
| `pilates`, `elliptical` | `yoga`, `cycling` |

### Plano gratuito

No free tier, os campos `calories` e `protein_g` vêm bloqueados. O backend estima calorias com `(carboidratos × 4) + (gordura × 9) + (proteína × 4)` quando o número não está disponível — alimentos muito proteicos (ex.: frango) podem parecer com valor baixo.

## Solução de problemas

| Sintoma | Causa provável | O que fazer |
|---------|----------------|-------------|
| Alimento sempre com **100 kcal** | Chave ausente, nome em português ou alimento não encontrado | Configure `API_NINJAS_KEY` e use nomes em inglês da tabela acima |
| Exercício com valor redondo (ex.: 150 para 30 min) | API não reconheceu o nome (`5 × minutos` de fallback) | Use `running`, `walking`, etc. |
| Erro 401 nos logs | Chave inválida ou expirada | Gere nova chave em [api-ninjas.com](https://api-ninjas.com) |
| Erro 429 nos logs | Limite do plano gratuito (60 req/mês) | Aguarde ou reduza testes |
| Porta 3000 ou 3001 em uso | Servidor já rodando | Encerre o processo antigo ou use outra porta |

Mais detalhes: [`backend/INSTRUÇÕES-TESTE.md`](backend/INSTRUÇÕES-TESTE.md) e [`backend/DOCUMENTACAO.md`](backend/DOCUMENTACAO.md).

## Testes

```bash
# Backend
cd backend && lein test

# Frontend
cd frontend && lein test
```

## Desenvolvimento com REPL

```bash
cd backend   # ou cd frontend
lein repl
```

## Princípios Funcionais Utilizados

- **Sem loops imperativos** — zero `loop`, `while`, `for`, `doseq`, `dotimes`
- **Imutabilidade** — dados em Atoms; `swap!`/`reset!` criam novos valores
- **Funções puras** — `map`, `filter`, `reduce` para transformações
- **Composição** — funções pequenas e middlewares encadeados com `->`
- **Separação de efeitos** — funções com `!` isolam side effects

## Tecnologias

| Componente | Tecnologia |
|-----------|-----------|
| Linguagem | Clojure |
| Web Backend | Ring + Compojure |
| Frontend HTML | Hiccup |
| HTTP Client | clj-http |
| JSON | Cheshire |
| API externa | API Ninjas (nutrition + caloriesburned) |

## Limitações Atuais

- Dados em memória (não persistem após reiniciar o servidor)
- Sem autenticação de usuário
- Sem suporte a múltiplos usuários
- Sem cache de respostas da API Ninjas
- Nomes de alimentos/exercícios devem estar em inglês
- Plano gratuito da API Ninjas limita campos nutricionais e volume de requisições

## Autores

- Clara Andrade — https://github.com/claraandrade20
- Paulo Alencar — https://github.com/opaulosaa

**Disciplina:** Programação Funcional (T300)  
**Instituição:** Universidade de Fortaleza  
**Data:** Junho de 2026

## Licença

Projeto acadêmico para fins educacionais.

```
Copyright © 2026 Clara Andrade, Paulo Alencar
Universidade de Fortaleza - Uso Acadêmico
```
