package br.com.cabeleleila.config;

import br.com.cabeleleila.model.Servico;
import br.com.cabeleleila.repository.ServicoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class DadosIniciais implements CommandLineRunner {

    private final ServicoRepository servicoRepository;

    public DadosIniciais(ServicoRepository servicoRepository) {
        this.servicoRepository = servicoRepository;
    }

    @Override
    public void run(String... args) {
        if (servicoRepository.count() > 0) {
            return;
        }

        servicoRepository.save(new Servico(null, "Corte Feminino", 55.0, 45));
        servicoRepository.save(new Servico(null, "Corte Masculino", 35.0, 30));
        servicoRepository.save(new Servico(null, "Escova", 40.0, 40));
        servicoRepository.save(new Servico(null, "Coloração", 120.0, 90));
        servicoRepository.save(new Servico(null, "Manicure", 25.0, 30));
    }
}
