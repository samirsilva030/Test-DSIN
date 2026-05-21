package br.com.cabeleleila.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void login_comSenhaCorreta_retornaToken() throws Exception {
        mockMvc.perform(post("/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senha\":\"leila2026\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autorizado").value(true))
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void login_comSenhaErrada_retorna401() throws Exception {
        mockMvc.perform(post("/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senha\":\"errada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").exists());
    }
}
