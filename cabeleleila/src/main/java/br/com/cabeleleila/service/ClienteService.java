package br.com.cabeleleila.service;

import br.com.cabeleleila.model.Cliente;
import br.com.cabeleleila.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public Cliente salvar(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    public Cliente buscarPorId(UUID id) {
        return clienteRepository.findById(id).orElse(null);
    }

    public void deletar(UUID id) {
        clienteRepository.deleteById(id);
    }
}
