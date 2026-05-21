package br.com.cabeleleila.service;

import br.com.cabeleleila.model.Cliente;
import br.com.cabeleleila.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    void salvar_deveRejeitarEmailDuplicado() {
        Cliente cliente = new Cliente(null, "João", "85988887777", "joao@test.com");
        when(clienteRepository.existsByEmailIgnoreCase("joao@test.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> clienteService.salvar(cliente));
        assertTrue(ex.getMessage().contains("e-mail"));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void salvar_deveNormalizarEmail() {
        Cliente cliente = new Cliente(null, "João", "85988887777", "  JOAO@Test.COM ");
        when(clienteRepository.existsByEmailIgnoreCase("joao@test.com")).thenReturn(false);
        when(clienteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Cliente salvo = clienteService.salvar(cliente);

        assertEquals("joao@test.com", salvo.getEmail());
    }

    @Test
    void buscarPorId_deveRetornarNullQuandoNaoExiste() {
        UUID id = UUID.randomUUID();
        when(clienteRepository.findById(id)).thenReturn(Optional.empty());

        assertNull(clienteService.buscarPorId(id));
    }
}
