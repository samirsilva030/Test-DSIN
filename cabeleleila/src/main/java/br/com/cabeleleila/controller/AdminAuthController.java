package br.com.cabeleleila.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminAuthController {

    @Value("${app.admin.senha}")
    private String senhaAdmin;

    @Value("${app.admin.token}")
    private String tokenAdmin;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String senha = body != null ? body.get("senha") : null;

        if (senha == null || !senha.equals(senhaAdmin)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Senha administrativa incorreta."));
        }

        return ResponseEntity.ok(Map.of(
                "autorizado", true,
                "token", tokenAdmin
        ));
    }
}
