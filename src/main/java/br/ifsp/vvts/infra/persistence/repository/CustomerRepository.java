package br.ifsp.vvts.infra.persistence.repository;

import br.ifsp.vvts.infra.persistence.entity.customer.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {
    @Query("SELECT c FROM CustomerEntity c WHERE c.cpf.number = :cpfNumber")
    Optional<CustomerEntity> findByCpfNumber(String cpfNumber);
}