# Calculadora de Calorias (Clojure REST API + Web)

Projeto acadêmico em **Clojure** para a disciplina de **Programação Funcional (T300)** na Universidade de Fortaleza. O sistema monitora consumo e gasto calórico por meio de um backend REST e uma interface web responsiva, consultando a **API Ninjas** para obter valores nutricionais.

```
trabfinal-api/
├── backend/                    # Servidor REST (porta 3000)
│   ├── src/trabfinal_api/
│   │   ├── handler.clj         # Rotas HTTP + CORS
│   │   ├── services.clj        # Lógica de negócio + API Ninjas
│   │   └── db.clj              # Estado em memória (atoms)
│   ├── test/
│   ├── project.clj
│   ├── README.md
│   ├── DOCUMENTACAO.md         # Documentação técnica do backend
│   └── INSTRUÇÕES-TESTE.md     # Guia de testes (Calva + curl)
│
├── frontend/                   # Interface web (porta 3001)
│   ├── src/trabfinal_app/
│   │   └── core.clj            # Páginas Hiccup + chamadas ao backend
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

```powershell
java -version
lein version
```

## Chave da API Ninjas

A chave está definida em `backend/src/trabfinal_api/services.clj` (variável privada `api-ninjas-key`). Substitua pelo valor da sua conta em [api-ninjas.com](https://api-ninjas.com).

Sem chave válida, ou quando a API falha, o sistema usa valores padrão:

- **Alimento:** `100` kcal
- **Exercício:** `5 × minutos` de duração

> Não commite chaves reais em repositórios públicos. Para produção, prefira variáveis de ambiente ou arquivos locais ignorados pelo Git.

## Como Executar

Use **dois terminais** — backend e frontend rodam em portas diferentes.

### Terminal 1 — Backend (API)

```powershell
cd backend
lein ring server-headless
```

Resultado: API em `http://localhost:3000`

Use `lein ring server` se quiser que o Ring abra o navegador automaticamente.

### Terminal 2 — Frontend (interface web)

```powershell
cd frontend
lein run
```

Resultado: aplicação em `http://localhost:3001`

O frontend consome o backend em `http://localhost:3000` (definido em `frontend/src/trabfinal_app/core.clj`).

## Funcionalidades

| Recurso | Descrição |
|---------|-----------|
| **Cadastro de Usuário** | Nome e peso (kg) — o peso entra no cálculo de exercícios |
| **Registro de Alimentos** | Alimento, quantidade (g), data — calorias via API Ninjas |
| **Registro de Exercícios** | Tipo, duração (min), data — gasto calórico via API Ninjas |
| **Extrato** | Transações filtradas por período |
| **Saldo** | Balanço calórico (consumo − gasto) |

## Endpoints da API

```
GET    /                           # Health check
POST   /api/usuario                # Registrar/atualizar usuário
GET    /api/usuario                # Consultar usuário
POST   /api/alimento               # Registrar alimento
POST   /api/exercicio              # Registrar exercício
GET    /api/extrato?inicio=&fim=   # Listar transações
GET    /api/saldo?inicio=&fim=     # Calcular balanço
```

## Modelo de Dados

```clojure
;; Usuário
{:nome "João Silva"
 :peso 75.5}

;; Alimento — nome em inglês para a API Ninjas
{:tipo :alimento
 :nome "chicken breast"
 :quantidade 150
 :data "2026-06-09"
 :calorias 165}

;; Exercício — nome em inglês; calorias negativas
{:tipo :exercicio
 :nome "running"
 :duracao 30
 :data "2026-06-09"
 :calorias -300}
```

## API Ninjas — nomes em inglês (obrigatório)

A API reconhece termos em **inglês**. Nomes em português (`frango`, `corrida`, `feijão`) costumam retornar lista vazia e o sistema grava o valor padrão — **não é o valor real**.

No formulário web, digite o termo exatamente como na coluna **Use na API**:

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

### Nomes que não funcionam

| Evite | Use em vez disso |
|-------|------------------|
| `weightlifting` | `weight lifting` |
| `stair climbing` | `stairs` |
| `pilates`, `elliptical` | `yoga`, `cycling` |

### Plano gratuito

No free tier, `calories` e `protein_g` podem vir bloqueados. O backend estima calorias com `(carboidratos × 4) + (gordura × 9) + (proteína × 4)` quando o número não está disponível — alimentos muito proteicos (ex.: frango) podem parecer com valor baixo.

## Solução de problemas

| Sintoma | Causa provável | O que fazer |
|---------|----------------|-------------|
| Alimento sempre com **100 kcal** | Nome em português ou alimento não encontrado | Use nomes em inglês da tabela acima |
| Exercício com valor redondo (ex.: 150 para 30 min) | API não reconheceu o nome (`5 × minutos` de fallback) | Use `running`, `walking`, etc. |
| Erro 401 nos logs | Chave inválida ou expirada | Atualize `api-ninjas-key` em `services.clj` |
| Erro 429 nos logs | Limite do plano gratuito (60 req/mês) | Aguarde ou reduza testes |
| Frontend sem dados | Backend parado ou porta errada | Inicie o backend na porta 3000 antes do frontend |
| Porta 3000 ou 3001 em uso | Servidor já rodando | Encerre o processo antigo |

Mais detalhes: [`backend/INSTRUÇÕES-TESTE.md`](backend/INSTRUÇÕES-TESTE.md) e [`backend/DOCUMENTACAO.md`](backend/DOCUMENTACAO.md).

## Testes

```powershell
# Backend
cd backend
lein test

# Frontend
cd frontend
lein test
```

## Desenvolvimento com REPL / Calva

```powershell
cd backend   # ou cd frontend
lein repl
```

Guia passo a passo com Calva: [`backend/INSTRUÇÕES-TESTE.md`](backend/INSTRUÇÕES-TESTE.md).

## Princípios Funcionais

- **Sem loops imperativos** — sem `loop`, `while`, `for`, `doseq`, `dotimes`
- **Imutabilidade** — dados em atoms; `swap!`/`reset!` criam novos valores
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
| API externa | API Ninjas (`/v1/nutrition`, `/v1/caloriesburned`) |

## Limitações

- Dados em memória (não persistem após reiniciar)
- Sem autenticação
- Um único usuário por instância do servidor
- Sem cache de respostas da API Ninjas
- Nomes de alimentos/exercícios devem estar em inglês
- Plano gratuito da API Ninjas limita campos e volume de requisições

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
