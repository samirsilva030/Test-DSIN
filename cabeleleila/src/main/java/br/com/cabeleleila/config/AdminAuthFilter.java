package br.com.cabeleleila.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AdminAuthFilter extends OncePerRequestFilter {

    public static final String HEADER_ADMIN = "X-Admin-Auth";

    @Value("${app.admin.token}")
    private String adminToken;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (requerAutenticacaoAdmin(request)) {
            String token = request.getHeader(HEADER_ADMIN);
            if (token == null || !token.equals(adminToken)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"erro\":\"Acesso restrito à administração do salão.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean requerAutenticacaoAdmin(HttpServletRequest request) {
        String metodo = request.getMethod();
        String uri = request.getRequestURI();

        if (HttpMethod.DELETE.matches(metodo)) {
            return uri.startsWith("/clientes")
                    || uri.startsWith("/servicos")
                    || uri.startsWith("/agendamentos");
        }

        return false;
    }
}
