package br.ifsp.vvts.infra.persistence.repository;

import br.ifsp.vvts.infra.persistence.entity.costumer.CostumerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CostumerRepository extends JpaRepository<CostumerEntity, Long> {
    @Query("SELECT c FROM CostumerEntity c WHERE c.cpf.number = :cpfNumber")
    Optional<CostumerEntity> findByCpfNumber(String cpfNumber);
}