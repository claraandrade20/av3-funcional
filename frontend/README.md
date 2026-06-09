# trabfinal-app — Frontend

Interface web em Clojure (Ring + Hiccup) para a **Calculadora de Calorias**. Consome a API do backend em `http://localhost:3000`.

## Pré-requisitos

- Backend rodando (`cd backend && lein ring server-headless`)
- Java JDK 8+ e Leiningen 2.0+

## Executar

```powershell
cd frontend
lein run
```

Abra `http://localhost:3001`.

## Páginas

| Rota | Função |
|------|--------|
| `/` | Início — saldo do dia e atalhos |
| `/usuario` | Cadastro de nome e peso |
| `/alimento` | Registrar alimento |
| `/exercicio` | Registrar exercício |
| `/extrato` | Histórico e saldo por período |

## Nomes em inglês na API

Os campos de alimento e exercício aceitam texto livre, mas a API Ninjas só reconhece termos em **inglês** (ex.: `chicken breast`, `running`). Veja as tabelas em [`../README.md`](../README.md).

## Licença

Projeto acadêmico — Universidade de Fortaleza, 2026.
