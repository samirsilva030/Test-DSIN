package br.com.cabeleleila.controller;

import br.com.cabeleleila.model.Servico;
import br.com.cabeleleila.service.ServicoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/servicos")
public class ServicoController {

    private final ServicoService servicoService;

    public ServicoController(ServicoService servicoService) {
        this.servicoService = servicoService;
    }

    @PostMapping
    public Servico salvar(@Valid @RequestBody Servico servico) {
        return servicoService.salvar(servico);
    }

    @GetMapping
    public List<Servico> listarTodos() {
        return servicoService.listarTodos();
    }

    @GetMapping("/{id}")
    public Servico buscarPorId(@PathVariable UUID id) {
        return servicoService.buscarPorId(id);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable UUID id) {
        servicoService.deletar(id);
    }
}
