package br.com.cabeleleila.service;

import br.com.cabeleleila.model.Servico;
import br.com.cabeleleila.repository.ServicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ServicoService {

    private final ServicoRepository servicoRepository;

    public ServicoService(ServicoRepository servicoRepository) {
        this.servicoRepository = servicoRepository;
    }

    public Servico salvar(Servico servico) {
        return servicoRepository.save(servico);
    }

    public List<Servico> listarTodos() {
        return servicoRepository.findAll();
    }

    public Servico buscarPorId(UUID id) {
        return servicoRepository.findById(id).orElse(null);
    }

    public void deletar(UUID id) {
        servicoRepository.deleteById(id);
    }
}
