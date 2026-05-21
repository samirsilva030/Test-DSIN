package br.com.cabeleleila.controller;

import br.com.cabeleleila.model.Agendamento;
import br.com.cabeleleila.service.AgendamentoService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/agendamentos")
public class AgendamentoController{
    private final AgendamentoService agendamentoService;

    public AgendamentoController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    @PostMapping
    public Agendamento salvar(@Valid @RequestBody Agendamento agendamento) {
        return agendamentoService.salvar(agendamento);
    }

    @GetMapping
    public List<Agendamento> listarTodos() {
        return agendamentoService.listarTodos();
    }

    @GetMapping("/historico")
    public List<Agendamento> listarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        return agendamentoService.listarPorPeriodo(inicio, fim);
    }

    @GetMapping("/cliente/{clienteId}")
    public List<Agendamento> listarPorCliente(@PathVariable UUID clienteId) {
        return agendamentoService.listarPorCliente(clienteId);
    }

    @GetMapping("/{id}")
    public Agendamento buscarPorId(@PathVariable UUID id) {
        return agendamentoService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public Agendamento alterarAgendamento(@PathVariable UUID id, @RequestBody Agendamento agendamento) {
        return agendamentoService.alterarAgendamento(id, agendamento);
    }

    @PatchMapping("/{id}/cancelar")
    public Agendamento cancelar(@PathVariable UUID id) {
        return agendamentoService.cancelar(id);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable UUID id) {
        agendamentoService.deletar(id);
    }
}
