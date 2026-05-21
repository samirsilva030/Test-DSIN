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
        if (cliente.getEmail() != null) {
            cliente.setEmail(cliente.getEmail().trim().toLowerCase());
        }
        if (cliente.getNome() != null) {
            cliente.setNome(cliente.getNome().trim());
        }
        if (cliente.getTelefone() != null) {
            cliente.setTelefone(cliente.getTelefone().trim());
        }

        if (clienteRepository.existsByEmailIgnoreCase(cliente.getEmail())) {
            throw new IllegalArgumentException("Já existe um cliente cadastrado com este e-mail.");
        }

        return clienteRepository.save(cliente);
    }

    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    public Cliente buscarPorId(UUID id) {
        return clienteRepository.findById(id).orElse(null);
    }

    public Cliente buscarPorEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return clienteRepository.findByEmailIgnoreCase(email.trim().toLowerCase()).orElse(null);
    }

    public void deletar(UUID id) {
        clienteRepository.deleteById(id);
    }
}
