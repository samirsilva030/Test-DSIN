package br.com.cabeleleila.service;

import br.com.cabeleleila.config.SalaoProperties;
import br.com.cabeleleila.model.Agendamento;
import br.com.cabeleleila.model.Cliente;
import br.com.cabeleleila.model.Servico;
import br.com.cabeleleila.repository.AgendamentoRepository;
import br.com.cabeleleila.repository.ClienteRepository;
import br.com.cabeleleila.repository.ServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private ServicoRepository servicoRepository;

    @Mock
    private SalaoProperties salaoProperties;

    @InjectMocks
    private AgendamentoService agendamentoService;

    private UUID clienteId;
    private UUID servicoId;
    private Cliente cliente;
    private Servico servico;

    @BeforeEach
    void setUp() {
        when(salaoProperties.getTelefone()).thenReturn("(85) 3222-1000");
        clienteId = UUID.randomUUID();
        servicoId = UUID.randomUUID();
        cliente = new Cliente(clienteId, "Maria", "85999998888", "maria@test.com");
        servico = new Servico(servicoId, "Corte", 50.0, 30);
    }

    @Test
    void salvar_deveRejeitarDataNoPassado() {
        Agendamento ag = agendamentoValido();
        ag.setDataHora(LocalDateTime.now().minusHours(1));

        assertThrows(IllegalArgumentException.class, () -> agendamentoService.salvar(ag));
        verify(agendamentoRepository, never()).save(any());
    }

    @Test
    void salvar_devePersistirComStatusAgendado() {
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(servicoRepository.findById(servicoId)).thenReturn(Optional.of(servico));
        when(agendamentoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Agendamento salvo = agendamentoService.salvar(agendamentoValido());

        assertEquals("AGENDADO", salvo.getStatus());
        verify(agendamentoRepository).save(any());
    }

    @Test
    void alterar_deveBloquearQuandoFaltamMenosDeDoisDias() {
        UUID agId = UUID.randomUUID();
        Agendamento existente = agendamentoValido();
        existente.setId(agId);
        existente.setDataHora(LocalDateTime.now().plusDays(1));

        when(agendamentoRepository.findById(agId)).thenReturn(Optional.of(existente));

        Agendamento novo = new Agendamento();
        novo.setDataHora(LocalDateTime.now().plusDays(5));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> agendamentoService.alterarAgendamento(agId, novo)
        );
        assertTrue(ex.getMessage().contains("2 dias"));
    }

    @Test
    void alterar_devePermitirComMaisDeDoisDias() {
        UUID agId = UUID.randomUUID();
        Agendamento existente = agendamentoValido();
        existente.setId(agId);
        existente.setDataHora(LocalDateTime.now().plusDays(5));

        when(agendamentoRepository.findById(agId)).thenReturn(Optional.of(existente));
        when(agendamentoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Agendamento novo = new Agendamento();
        novo.setDataHora(LocalDateTime.now().plusDays(7));

        Agendamento atualizado = agendamentoService.alterarAgendamento(agId, novo);

        assertEquals(novo.getDataHora(), atualizado.getDataHora());
    }

    @Test
    void cancelar_deveBloquearComMenosDeDoisDias() {
        UUID agId = UUID.randomUUID();
        Agendamento existente = agendamentoValido();
        existente.setId(agId);
        existente.setDataHora(LocalDateTime.now().plusHours(12));

        when(agendamentoRepository.findById(agId)).thenReturn(Optional.of(existente));

        assertThrows(IllegalStateException.class, () -> agendamentoService.cancelar(agId));
    }

    @Test
    void cancelar_deveMarcarStatusCancelado() {
        UUID agId = UUID.randomUUID();
        Agendamento existente = agendamentoValido();
        existente.setId(agId);
        existente.setDataHora(LocalDateTime.now().plusDays(4));

        when(agendamentoRepository.findById(agId)).thenReturn(Optional.of(existente));
        when(agendamentoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Agendamento cancelado = agendamentoService.cancelar(agId);

        assertEquals("CANCELADO", cancelado.getStatus());
    }

    @Test
    void listarPorPeriodo_deveRejeitarFimAntesDoInicio() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(5);
        LocalDateTime fim = LocalDateTime.now().plusDays(2);

        assertThrows(IllegalArgumentException.class, () -> agendamentoService.listarPorPeriodo(inicio, fim));
    }

    private Agendamento agendamentoValido() {
        Agendamento ag = new Agendamento();
        ag.setDataHora(LocalDateTime.now().plusDays(3));
        ag.setCliente(new Cliente(clienteId, null, null, null));
        ag.setServicos(List.of(new Servico(servicoId, null, null, null)));
        return ag;
    }
}
