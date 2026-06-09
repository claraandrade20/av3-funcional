# trabfinal-api — Backend

API REST em Clojure (Ring + Compojure) para a **Calculadora de Calorias**. Consulta a [API Ninjas](https://api.ninjas.com) para obter valores nutricionais de alimentos e gasto calórico de exercícios.

Documentação completa: [`DOCUMENTACAO.md`](DOCUMENTACAO.md) · Guia de testes: [`INSTRUÇÕES-TESTE.md`](INSTRUÇÕES-TESTE.md)

## Pré-requisitos

- Java JDK 8+
- [Leiningen](https://github.com/technomancy/leiningen) 2.0+

```powershell
java -version
lein version
```

## Executar

```powershell
cd backend
lein ring server-headless
```

A API fica em `http://localhost:3000`. Use `lein ring server` se quiser que o Ring abra o navegador.

## Chave da API Ninjas

A chave está definida em `src/trabfinal_api/services.clj` (variável privada `api-ninjas-key`). Para trocar, edite esse arquivo com sua chave de [api-ninjas.com](https://api-ninjas.com).

## Endpoints

| Método | Rota | Descrição |
|--------|------|-----------|
| `GET` | `/` | Health check |
| `POST` | `/api/usuario` | Cadastrar/atualizar usuário |
| `GET` | `/api/usuario` | Consultar usuário |
| `POST` | `/api/alimento` | Registrar alimento |
| `POST` | `/api/exercicio` | Registrar exercício |
| `GET` | `/api/extrato?inicio=&fim=` | Listar transações |
| `GET` | `/api/saldo?inicio=&fim=` | Balanço calórico |

## Testes

```powershell
lein test
```

## Licença

Projeto acadêmico — Universidade de Fortaleza, 2026.
