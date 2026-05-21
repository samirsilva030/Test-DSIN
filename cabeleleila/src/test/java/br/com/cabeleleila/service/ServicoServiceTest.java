package br.com.cabeleleila.service;

import br.com.cabeleleila.model.Servico;
import br.com.cabeleleila.repository.ServicoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {

    @Mock
    private ServicoRepository servicoRepository;

    @InjectMocks
    private ServicoService servicoService;

    @Test
    void salvar_devePersistirServico() {
        Servico servico = new Servico(null, "Escova", 40.0, 35);
        when(servicoRepository.save(servico)).thenReturn(servico);

        Servico salvo = servicoService.salvar(servico);

        assertEquals("Escova", salvo.getNome());
        assertEquals(40.0, salvo.getPreco());
    }
}
