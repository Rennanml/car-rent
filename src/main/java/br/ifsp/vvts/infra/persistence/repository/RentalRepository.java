package br.ifsp.vvts.infra.persistence.repository;

import br.ifsp.vvts.domain.model.car.LicensePlate;
import br.ifsp.vvts.domain.model.rental.Rental;
import br.ifsp.vvts.domain.model.rental.RentalPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    boolean existsByCarLicensePlateAndPeriodOverlaps(LicensePlate licensePlate, RentalPeriod period);
}