# Calculadora de Calorias

> Projeto final da disciplina de Programação Funcional (T300) - Universidade de Fortaleza  

## Sobre o Projeto

A Calculadora de Calorias é uma aplicação completa desenvolvida em Clojure que permite aos usuários monitorar seu consumo e gasto calórico através do registro de alimentos consumidos e atividades físicas realizadas. O sistema ajuda no planejamento de uma rotina alimentar e de exercícios para alcançar objetivos de saúde, como ganho de massa muscular ou perda de peso.

## Justificativa

Com a crescente preocupação com a saúde, as pessoas buscam ferramentas para:
- **Monitorar o consumo calórico** - identificar hábitos alimentares e fazer escolhas mais conscientes
- **Acompanhar a perda calórica** - identificar quais atividades físicas são mais eficazes
- **Planejar alimentação e exercícios** - tomar decisões assertivas para alcançar objetivos de saúde

## Arquitetura

O projeto é dividido em dois componentes principais:

```
┌──────────────────────────────────────────────────────────────┐
│                  Calculadora de Calorias                      │
├────────────────────────────┬─────────────────────────────────┤
│         Front-end          │           Back-end              │
├────────────────────────────┼─────────────────────────────────┤
│                            │                                 │
│  ┌──────────────────┐     │      ┌────────────┐            │
│  │   Interface com  │◄────┼─────►│    API     │            │
│  │     Usuário      │     │      └─────┬──────┘            │
│  └──────────────────┘     │            │                    │
│                            │  ┌─────────┴─────────┐         │
│         HTTP/JSON          │  │                   │         │
│                            │  ▼                   ▼         │
│                            │ ┌──────────┐  ┌──────────┐    │
│                            │ │   API    │  │   API    │    │
│                            │ │ Externa  │  │ Externa  │    │
│                            │ │Alimentos │  │Exercícios│    │
│                            │ └──────────┘  └──────────┘    │
└────────────────────────────┴─────────────────────────────────┘
```

### Backend
- **Linguagem**: Clojure
- **Framework**: Ring + Compojure
- **Armazenamento**: Em memória (Atoms)
- **Comunicação**: HTTP/JSON

### Frontend
- **Linguagem**: Clojure
- **Interface**: Desktop (linha de comando ou gráfica)
- **Comunicação**: Cliente HTTP para a API do backend

## 🛠️ Tecnologias Utilizadas

- **Clojure 1.10+** - Linguagem de programação funcional
- **Leiningen** - Gerenciador de projetos e dependências
- **Ring** - Servidor HTTP para Clojure
- **Compojure** - Biblioteca de roteamento web
- **APIs Externas**:
  - [Rapid API](https://rapidapi.com/) - Para informações nutricionais de alimentos
  - [API Ninjas](https://api-ninjas.com/) - Para cálculo de calorias de exercícios

## Estrutura do Projeto

```
trabfinal-api/
├── backend/
│   ├── src/
│   │   └── trabfinal_api/
│   │       └── handler.clj      # Rotas e lógica da API
│   ├── test/
│   │   └── trabfinal_api/
│   │       └── handler_test.clj # Testes do backend
│   ├── resources/
│   │   └── public/              # Arquivos públicos
│   └── project.clj              # Configuração do projeto backend
│
├── frontend/
│   ├── src/
│   │   └── trabfinal_app/
│   │       └── core.clj         # Interface com usuário
│   ├── test/
│   │   └── trabfinal_app/
│   │       └── core_test.clj    # Testes do frontend
│   └── project.clj              # Configuração do projeto frontend
│
└── README.md                    # Este arquivo
```

## Como Executar

### Pré-requisitos

1. **Java JDK 8+** instalado
   ```bash
   java -version
   ```

2. **Leiningen** instalado
   ```bash
   lein version
   ```
   Se não tiver instalado, siga as instruções em [leiningen.org](https://leiningen.org/)

### Executando o Backend (API)

1. Navegue até o diretório do backend:
   ```bash
   cd backend
   ```

2. Instale as dependências:
   ```bash
   lein deps
   ```

3. Inicie o servidor:
   ```bash
   lein ring server
   ```
   
   A API estará disponível em `http://localhost:3000`

4. Para executar em modo headless (sem abrir o navegador):
   ```bash
   lein ring server-headless
   ```

5. Para executar os testes:
   ```bash
   lein test
   ```

### Executando o Frontend

1. Navegue até o diretório do frontend:
   ```bash
   cd frontend
   ```

2. Instale as dependências:
   ```bash
   lein deps
   ```

3. Execute a aplicação:
   ```bash
   lein run
   ```

4. Para executar os testes:
   ```bash
   lein test
   ```

### Executando em Desenvolvimento

Para desenvolvimento, você pode usar o REPL:

**Backend:**
```bash
cd backend
lein repl
```

**Frontend:**
```bash
cd frontend
lein repl
```

## Funcionalidades

O sistema permite realizar as seguintes operações:

### 1. Cadastro de Usuário
- Registrar dados pessoais: altura, peso, idade e sexo
- Consultar dados cadastrados

### 2. Registro de Alimentos
- Informar alimento consumido
- Informar data do consumo
- Informar quantidade consumida
- Cálculo automático de calorias via API externa

### 3. Registro de Atividades Físicas
- Informar atividade realizada
- Informar data da realização
- Informar tempo de duração
- Cálculo automático de calorias perdidas via API externa

### 4. Consultas
- **Extrato de transações**: visualizar todas as transações em um período
- **Saldo de calorias**: consultar saldo (ganho - perda) em um período

## Endpoints da API (a implementar)

### Usuário
- `POST /api/usuario` - Cadastrar dados do usuário
- `GET /api/usuario` - Consultar dados do usuário

### Alimentos
- `POST /api/alimento` - Registrar consumo de alimento

### Exercícios
- `POST /api/exercicio` - Registrar realização de exercício

### Relatórios
- `GET /api/extrato?inicio=YYYY-MM-DD&fim=YYYY-MM-DD` - Obter extrato de transações
- `GET /api/saldo?inicio=YYYY-MM-DD&fim=YYYY-MM-DD` - Obter saldo de calorias

## Estrutura de Dados

As transações são representadas como mapas (hash-maps) e armazenadas em listas:

```clojure
;; Exemplo de transação de alimento
{:tipo :alimento
 :nome "Banana"
 :data "2026-06-01"
 :quantidade 100  ; gramas
 :calorias 89}

;; Exemplo de transação de exercício
{:tipo :exercicio
 :nome "Corrida"
 :data "2026-06-01"
 :duracao 30  ; minutos
 :calorias -300}

;; Dados do usuário
{:altura 170   ; cm
 :peso 70      ; kg
 :idade 25
 :sexo "M"}
```

## Princípios de Programação Funcional

O projeto segue os conceitos fundamentais da programação funcional:

- ✅ **Funções de ordem superior** - map, filter, reduce
- ✅ **Pureza** - funções sem efeitos colaterais
- ✅ **Recursão de cauda** - substituição de loops
- ✅ **Imutabilidade** - estruturas de dados imutáveis
- ✅ **Composição de funções** - funções pequenas e compostas
- ❌ **Sem loops imperativos** - não usar LOOP, WHILE, FOR, DOSEQ, DOTIMES

## 📚 Referências

- Livro: **Programação Funcional: Uma Introdução em Clojure** (Capítulos 9 a 13)
- [Documentação do Clojure](https://clojure.org/)
- [Ring Documentation](https://github.com/ring-clojure/ring)
- [Compojure Documentation](https://github.com/weavejester/compojure)


## 🤝 Autores

- [Maria Clara Andrade Gomes]
- [Paulo Alencar]


## 📄 Licença

Este é um projeto acadêmico desenvolvido para fins educacionais na disciplina de Programação Funcional da Universidade de Fortaleza.

---

**Universidade de Fortaleza**  
Disciplina: Programação Funcional (T300)  
