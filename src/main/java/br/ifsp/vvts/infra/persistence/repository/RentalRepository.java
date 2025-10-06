package br.ifsp.vvts.infra.persistence.repository;

import br.ifsp.vvts.infra.persistence.entity.car.LicensePlateEmbeddable;
import br.ifsp.vvts.infra.persistence.entity.rental.RentalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface RentalRepository extends JpaRepository<RentalEntity, Long> {

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN TRUE ELSE FALSE END " +
            "FROM RentalEntity r " +
            "WHERE r.car.licensePlate = :licensePlate " +
            "AND r.status = 'ACTIVE' " +
            "AND (:startDate < r.endDate AND :endDate > r.startDate)")
    boolean existsByCarLicensePlateAndPeriodOverlaps(@Param("licensePlate") LicensePlateEmbeddable licensePlate,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);
}