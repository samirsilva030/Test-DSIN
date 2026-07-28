package br.com.cabeleleila.controller;

import br.com.cabeleleila.model.Cliente;
import br.com.cabeleleila.model.Servico;
import br.com.cabeleleila.repository.AgendamentoRepository;
import br.com.cabeleleila.repository.ClienteRepository;
import br.com.cabeleleila.repository.ServicoRepository;
import org.springframework.test.context.ActiveProfiles;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AgendamentoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ServicoRepository servicoRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @BeforeEach
    void limpar() {
        agendamentoRepository.deleteAll();
        clienteRepository.deleteAll();
        if (servicoRepository.count() == 0) {
            servicoRepository.save(new Servico(null, "Corte Teste", 45.0, 30));
        }
    }

    @Test
    void fluxoAgendamento_comClienteEServicoExistentes() throws Exception {
        Cliente cliente = clienteRepository.save(new Cliente(null, "Teste", "85999990000", "teste@dsin.com"));
        var servico = servicoRepository.findAll().stream().findFirst().orElseThrow();

        String payload = objectMapper.writeValueAsString(Map.of(
                "dataHora", LocalDateTime.now().plusDays(4).withNano(0).toString(),
                "cliente", Map.of("id", cliente.getId().toString()),
                "servicos", List.of(Map.of("id", servico.getId().toString()))
        ));

        mockMvc.perform(post("/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AGENDADO"))
                .andExpect(jsonPath("$.valorTotal").isNumber());
    }

    @Test
    void alterar_deveBloquearComMenosDeDoisDias() throws Exception {
        Cliente cliente = clienteRepository.save(new Cliente(null, "Teste", "85999990000", "bloqueio@dsin.com"));
        var servico = servicoRepository.findAll().stream().findFirst().orElseThrow();

        String criar = objectMapper.writeValueAsString(Map.of(
                "dataHora", LocalDateTime.now().plusDays(1).withNano(0).toString(),
                "cliente", Map.of("id", cliente.getId().toString()),
                "servicos", List.of(Map.of("id", servico.getId().toString()))
        ));

        String corpo = mockMvc.perform(post("/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criar))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(corpo).get("id").asText();

        String alterar = objectMapper.writeValueAsString(Map.of(
                "dataHora", LocalDateTime.now().plusDays(3).withNano(0).toString()
        ));

        mockMvc.perform(put("/agendamentos/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(alterar))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", containsString("2 dias")));
    }

    @Test
    void historico_deveRetornarListaNoPeriodo() throws Exception {
        LocalDateTime inicio = LocalDateTime.now().minusDays(1);
        LocalDateTime fim = LocalDateTime.now().plusDays(10);

        mockMvc.perform(get("/agendamentos/historico")
                        .param("inicio", inicio.toString())
                        .param("fim", fim.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
