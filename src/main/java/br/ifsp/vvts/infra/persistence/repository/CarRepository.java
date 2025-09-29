package br.ifsp.vvts.infra.persistence.repository;

import br.ifsp.vvts.infra.persistence.entity.car.CarEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<CarEntity, Long> {
    @Query("SELECT c FROM CarEntity c WHERE c.licensePlate.value = :licensePlate")
    Optional<CarEntity> findByLicensePlate(String licensePlate);
}