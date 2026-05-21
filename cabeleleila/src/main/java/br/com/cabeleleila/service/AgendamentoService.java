package br.com.cabeleleila.service;

import br.com.cabeleleila.model.Agendamento;
import br.com.cabeleleila.model.Cliente;
import br.com.cabeleleila.model.Servico;
import br.com.cabeleleila.repository.AgendamentoRepository;
import br.com.cabeleleila.repository.ClienteRepository;
import br.com.cabeleleila.repository.ServicoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final ClienteRepository clienteRepository;
    private final ServicoRepository servicoRepository;

    public AgendamentoService(
            AgendamentoRepository agendamentoRepository,
            ClienteRepository clienteRepository,
            ServicoRepository servicoRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.clienteRepository = clienteRepository;
        this.servicoRepository = servicoRepository;
    }

    public Agendamento salvar(Agendamento agendamento) {
        if (agendamento.getDataHora() == null || agendamento.getDataHora().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Não é possível realizar um agendamento no passado ou com data nula.");
        }

        agendamento.setCliente(resolverCliente(agendamento.getCliente()));
        agendamento.setServicos(resolverServicos(agendamento.getServicos()));
        agendamento.setValorTotal(calcularValorTotal(agendamento.getServicos()));
        agendamento.setStatus("AGENDADO");

        return agendamentoRepository.save(agendamento);
    }

    public List<Agendamento> listarTodos() {
        return agendamentoRepository.findAll();
    }

    public List<Agendamento> listarPorCliente(UUID clienteId) {
        if (clienteId == null) {
            throw new IllegalArgumentException("Cliente é obrigatório.");
        }
        return agendamentoRepository.findByClienteIdOrderByDataHoraDesc(clienteId);
    }

    public List<Agendamento> listarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null || fim == null) {
            throw new IllegalArgumentException("As datas de início e fim devem ser informadas.");
        }
        if (fim.isBefore(inicio)) {
            throw new IllegalArgumentException("A data fim deve ser posterior à data início.");
        }
        return agendamentoRepository.findByDataHoraBetweenOrderByDataHoraAsc(inicio, fim);
    }

    public Agendamento buscarPorId(UUID id) {
        return agendamentoRepository.findById(id).orElse(null);
    }

    public void deletar(UUID id) {
        agendamentoRepository.deleteById(id);
    }

    public Agendamento cancelar(UUID id) {
        Agendamento agendamento = buscarPorId(id);
        if (agendamento == null) {
            throw new IllegalArgumentException("Agendamento não encontrado.");
        }
        validarPrazoAlteracao(agendamento.getDataHora());
        agendamento.setStatus("CANCELADO");
        return agendamentoRepository.save(agendamento);
    }

    public Agendamento alterarAgendamento(UUID id, Agendamento novoAgendamento) {
        Agendamento agendamentoExistente = buscarPorId(id);

        if (agendamentoExistente == null) {
            throw new IllegalArgumentException("Agendamento não encontrado.");
        }

        if ("CANCELADO".equals(agendamentoExistente.getStatus())) {
            throw new IllegalStateException("Não é possível alterar um agendamento cancelado.");
        }

        validarPrazoAlteracao(agendamentoExistente.getDataHora());

        LocalDateTime agora = LocalDateTime.now();

        if (novoAgendamento.getDataHora() != null && novoAgendamento.getDataHora().isBefore(agora)) {
            throw new IllegalArgumentException("A nova data não pode ser no passado.");
        }

        if (novoAgendamento.getDataHora() != null) {
            agendamentoExistente.setDataHora(novoAgendamento.getDataHora());
        }
        if (novoAgendamento.getServicos() != null && !novoAgendamento.getServicos().isEmpty()) {
            agendamentoExistente.setServicos(resolverServicos(novoAgendamento.getServicos()));
            agendamentoExistente.setValorTotal(calcularValorTotal(agendamentoExistente.getServicos()));
        }

        return agendamentoRepository.save(agendamentoExistente);
    }

    private void validarPrazoAlteracao(LocalDateTime dataAgendada) {
        LocalDateTime agora = LocalDateTime.now();
        if (dataAgendada.minusDays(2).isBefore(agora)) {
            throw new IllegalStateException(
                    "Alterações só podem ser feitas até 2 dias antes do agendamento. Entre em contato por telefone."
            );
        }
    }

    private Cliente resolverCliente(Cliente cliente) {
        if (cliente == null || cliente.getId() == null) {
            throw new IllegalArgumentException("Cliente é obrigatório para o agendamento.");
        }
        return clienteRepository.findById(cliente.getId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado."));
    }

    private List<Servico> resolverServicos(List<Servico> servicos) {
        if (servicos == null || servicos.isEmpty()) {
            throw new IllegalArgumentException("Selecione ao menos um serviço.");
        }

        List<Servico> resolvidos = new ArrayList<>();
        for (Servico s : servicos) {
            if (s.getId() == null) {
                throw new IllegalArgumentException("Serviço inválido.");
            }
            resolvidos.add(servicoRepository.findById(s.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado: " + s.getId())));
        }
        return resolvidos;
    }

    private double calcularValorTotal(List<Servico> servicos) {
        return servicos.stream().mapToDouble(Servico::getPreco).sum();
    }
}
