# Checklist — Edital DSIN (Cabeleleila Leila)

Use este arquivo para conferir antes de enviar o e-mail.

## Funcionalidades do cliente (Fundamental)

- [x] Agendamento online
- [x] Um ou mais serviços por agendamento
- [x] Alteração pelo sistema até 2 dias antes
- [x] Bloqueio com menos de 2 dias + orientação telefone
- [x] Histórico por período (data início / fim)

## Critérios de avaliação

- [x] Usabilidade (áreas separadas cliente/admin, mensagens, toasts)
- [x] MVC / camadas (ver `ARQUITETURA.md`)
- [x] Código organizado (Controller → Service → Repository)
- [x] Validações backend e frontend
- [x] Testes unitários e integração (`mvn test`)

## Extras (Plus)

- [x] Valor total do agendamento
- [x] Duração total estimada (front)
- [x] Cancelamento pelo cliente
- [x] E-mail único
- [x] Admin com senha
- [x] Documentação (README, INFORMACOES, ARQUITETURA)

## Entrega no e-mail (você precisa fazer manualmente)

- [ ] Link do repositório Git (ou ZIP)
- [ ] Pasta `entrega/screenshots/` com prints das telas
- [ ] Vídeo de demonstração em `entrega/` (ex.: `demo.mp4`)
- [ ] Preencher nome e link em `INFORMACOES.txt`

## Como validar rapidamente

```bash
mvn test
mvn spring-boot:run
# http://localhost:8080
```
