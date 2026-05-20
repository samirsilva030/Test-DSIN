package br.com.cabeleleila.service;

import br.com.cabeleleila.model.Agendamento;
import br.com.cabeleleila.repository.AgendamentoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;

    public AgendamentoService(AgendamentoRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
    }

    public Agendamento salvar(Agendamento agendamento) {
        if (agendamento.getDataHora() == null || agendamento.getDataHora().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Não é possível realizar um agendamento no passado ou com data nula.");
        }
        agendamento.setStatus("AGENDADO");
        return agendamentoRepository.save(agendamento);
    }

    public List<Agendamento> listarTodos() {
        return agendamentoRepository.findAll();
    }

    public List<Agendamento> listarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null || fim == null) {
            throw new IllegalArgumentException("As datas de início e fim devem ser informadas.");
        }
        return agendamentoRepository.findByDataHoraBetweenOrderByDataHoraAsc(inicio, fim);
    }

    public Agendamento buscarPorId(UUID id) {
        return agendamentoRepository.findById(id).orElse(null);
    }

    public void deletar(UUID id) {
        agendamentoRepository.deleteById(id);
    }

    public Agendamento alterarAgendamento(UUID id, Agendamento novoAgendamento) {
        Agendamento agendamentoExistente = buscarPorId(id);

        if (agendamentoExistente == null) {
            throw new IllegalArgumentException("Agendamento não encontrado.");
        }

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime dataAgendada = agendamentoExistente.getDataHora();

        if (dataAgendada.minusDays(2).isBefore(agora)) {
            throw new IllegalStateException(
                    "Alterações só podem ser feitas até 2 dias antes do agendamento. Entre em contato por telefone."
            );
        }

        if (novoAgendamento.getDataHora() != null && novoAgendamento.getDataHora().isBefore(agora)) {
            throw new IllegalArgumentException("A nova data não pode ser no passado.");
        }

        if (novoAgendamento.getDataHora() != null) {
            agendamentoExistente.setDataHora(novoAgendamento.getDataHora());
        }
        if (novoAgendamento.getServicos() != null && !novoAgendamento.getServicos().isEmpty()) {
            agendamentoExistente.setServicos(novoAgendamento.getServicos());
        }

        return agendamentoRepository.save(agendamentoExistente);
    }
}
