# Calculadora de Calorias (Clojure REST API + Web)

Projeto acadêmico desenvolvido em **Clojure** para a disciplina de **Programação Funcional (T300)** na Universidade de Fortaleza. O sistema monitora consumo e gasto calórico através de um backend REST e interface web responsiva.


```
trabfinal-api/
├── backend/                    # Servidor REST (API)
│   ├── src/trabfinal_api/
│   │   ├── handler.clj         # Rotas HTTP
│   │   ├── services.clj        # Lógica de negócio
│   │   └── db.clj              # Gerenciamento de dados (Atoms)
│   ├── test/
│   └── project.clj
│
├── frontend/                   # Aplicação Web
│   ├── src/trabfinal_app/
│   │   └── core.clj            # Interface com usuário (Hiccup)
│   ├── test/
│   └── project.clj
│
└── README.md                   # Este arquivo
```

## 🚀 Como Executar

A arquitetura exige **dois terminais simultâneos**:

### Terminal 1 - Backend (Servidor da API)

```bash
cd backend
lein ring server
```

*Opcional: use `lein ring server-headless` para não abrir navegador*

**Resultado esperado:** API disponível em `http://localhost:3000`

### Terminal 2 - Frontend (Interface Web)

```bash
cd frontend
lein run
```

**Resultado esperado:** Aplicação em `http://localhost:3001`

## 🎯 Funcionalidades

| Recurso | Descrição |
|---------|-----------|
| **Cadastro de Usuário** | Nome, peso - dados pessoais |
| **Registro de Alimentos** | Alimento, quantidade (g), data |
| **Registro de Exercícios** | Tipo, duração (min), data |
| **Extrato** | Lista todas as transações em período |
| **Saldo** | Balanço calórico (consumo - gasto) |

## 🏗 Principais Endpoints

```
POST   /api/usuario              # Registrar usuário
GET    /api/usuario              # Consultar usuário
POST   /api/alimento             # Registrar alimento
POST   /api/exercicio            # Registrar exercício
GET    /api/extrato              # Listar transações
GET    /api/saldo                # Calcular balanço
```

## 📊 Modelo de Dados

```clojure
;; Alimento
{:tipo :alimento
 :nome "Frango Grelhado"
 :quantidade 150
 :data "2026-06-05"
 :calorias 165}

;; Exercício
{:tipo :exercicio
 :nome "Corrida"
 :duracao 30
 :data "2026-06-05"
 :calorias -300}

;; Usuário
{:nome "João Silva"
 :peso 75.5}
```

## 🔧 Pré-requisitos

| Software | Versão |
|----------|--------|
| Java JDK | 8+ |
| Leiningen | 2.0+ |

```bash
# Verificar instalações
java -version
lein version
```

## ⚙ Testes

```bash
# Backend
cd backend && lein test

# Frontend
cd frontend && lein test
```

## 💻 Desenvolvimento com REPL

```bash
cd backend   # ou cd frontend
lein repl
```

## 📐 Princípios Funcionais Utilizados

- ✅ **Sem Loops Imperativos**: Zero `loop`, `while`, `for`, `doseq`, `dotimes`
- ✅ **Imutabilidade Total**: Dados armazenados em Atoms
- ✅ **Funções Puras**: map, filter, reduce para transformações
- ✅ **Composição**: Funções pequenas e reutilizáveis
- ✅ **Recursão**: Uso de `recur` para iterações

## 🔌 Integrações Externas

- **RapidAPI** - Informações nutricionais
- **API Ninjas** - Cálculo de gasto calórico

## 📚 Tecnologias

| Componente | Tecnologia |
|-----------|-----------|
| Linguagem | Clojure |
| Web Backend | Ring + Compojure |
| Frontend HTML | Hiccup |
| HTTP Client | clj-http |
| JSON | Cheshire |

## 🚫 Limitações Atuais

- Dados em memória (não persiste após reiniciar)
- Sem autenticação de usuário
- Sem suporte a múltiplos usuários
- Sem cache de APIs externas

## 👥 Autores

- Clara Andrade - https://github.com/claraandrade20
- Paulo Alencar - https://github.com/opaulosaa

**Disciplina:** Programação Funcional (T300)  
**Instituição:** Universidade de Fortaleza  
**Data:** Junho de 2026

## 📄 Licença

Projeto acadêmico para fins educacionais.

```
Copyright © 2026 Clara Andrade, Paulo Alencar
Universidade de Fortaleza - Uso Acadêmico
```

---

**Repositório:** https://github.com/claraandrade20/av3-funcional

