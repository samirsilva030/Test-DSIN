package br.com.cabeleleila.repository;


import br.com.cabeleleila.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClienteRepository extends JpaRepository <Cliente, UUID>{

}
