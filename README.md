# Cabeleleila Leila — Salão de Beleiza

Sistema de agendamento online

## Tecnologias

| Camada | Stack |
|--------|--------|
| Backend | Java 21, Spring Boot 3, Spring Data JPA, Bean Validation |
| Banco | H2 (em memória) |
| Frontend | HTML5, CSS3, JavaScript (vanilla) |
| Testes | JUnit 5, Mockito, Spring MockMvc |
| Arquitetura | MVC em camadas (Controller → Service → Repository) |

Acesse: **http://localhost:8080**

## Áreas do sistema

- **Cliente** (`/cliente.html`) — cadastro, login por e-mail, agendamento de múltiplos serviços, alteração, cancelamento e histórico
- **Admin** (`/admin-login.html`) — senha: `leila2026` (configurável em `application.properties`)

## Requisitos do teste (checklist)

| Requisito | Status |
|-----------|--------|
| Agendar um ou mais serviços | ✅ |
| Alterar até 2 dias antes | ✅ (backend + frontend) |
| Bloqueio com menos de 2 dias (telefone) | ✅ |
| Histórico por período | ✅ |
| Área cliente / admin separadas | ✅ |
| Admin protegido por senha | ✅ |

## Diferenciais (Plus)

- Cálculo automático do **valor total** do agendamento
- **Cancelamento** pelo cliente (status `CANCELADO`, mesma regra dos 2 dias)
- E-mail único por cliente
- Validações backend (Jakarta Validation) e frontend
- **Testes unitários e de integração**
- Dados iniciais de serviços para demonstração


## API principal

```
POST   /clientes
GET    /clientes/email/{email}
POST   /agendamentos
GET    /agendamentos/cliente/{clienteId}
GET    /agendamentos/historico?inicio=&fim=
PUT    /agendamentos/{id}
PATCH  /agendamentos/{id}/cancelar
POST   /admin/login
```
