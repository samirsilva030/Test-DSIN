# Arquitetura do Sistema — Cabeleleila Leila

## Padrão utilizado

O projeto segue **arquitetura em camadas (3-tier)** com **MVC adaptado para API REST**:

| Camada | Pacote / pasta | Responsabilidade |
|--------|----------------|------------------|
| **View** | `src/main/resources/static/` | Interface HTML/CSS/JS (cliente e admin) |
| **Controller** | `controller/` | Endpoints REST, recebe HTTP, retorna JSON |
| **Service** | `service/` | Regras de negócio (2 dias, valor total, validações) |
| **Repository** | `repository/` | Persistência JPA (H2) |
| **Model** | `model/` | Entidades JPA (`Cliente`, `Servico`, `Agendamento`) |

O frontend é **desacoplado**: não há JSP/Thymeleaf; a View roda no navegador e consome a API.

## Fluxo de um agendamento

```
Cliente (browser) → POST /agendamentos (JSON)
    → AgendamentoController
    → AgendamentoService (valida data, resolve cliente/serviços, calcula valor)
    → AgendamentoRepository → H2
```

## Regra dos 2 dias

Implementada em `AgendamentoService.validarPrazoAlteracao()`:

- Permitido alterar/cancelar online se faltam **mais de 48 horas** para o horário.
- Caso contrário: `IllegalStateException` com telefone do salão (`app.salao.telefone`).

## Segurança administrativa

- Login: `POST /admin/login`
- Operações de exclusão exigem header `X-Admin-Auth` (`AdminAuthFilter`).

## Testes

- **Unitários**: `service/*Test` (Mockito) — regras isoladas
- **Integração**: `controller/*IntegrationTest` (MockMvc + Spring context)
