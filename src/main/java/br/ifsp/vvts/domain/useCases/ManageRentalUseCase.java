package br.ifsp.vvts.domain.useCases;

import br.ifsp.vvts.domain.model.rental.Rental;
import br.ifsp.vvts.domain.model.rental.RentalPeriod;
import br.ifsp.vvts.domain.model.rental.RentalStatus;
import br.ifsp.vvts.infra.persistence.entity.car.CarEntity;
import br.ifsp.vvts.infra.persistence.entity.customer.CustomerEntity;
import br.ifsp.vvts.infra.persistence.entity.rental.RentalEntity;
import br.ifsp.vvts.infra.persistence.mapper.RentalMapper;
import br.ifsp.vvts.infra.persistence.repository.RentalRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ManageRentalUseCase {

    private final RentalRepository rentalRepository;
    private final RentalMapper rentalMapper;

    public ManageRentalUseCase(RentalRepository rentalRepository, RentalMapper rentalMapper) {
        this.rentalRepository = rentalRepository;
        this.rentalMapper = rentalMapper;
    }

    @Transactional
    public Rental createRental(CustomerEntity customerEntity, CarEntity carEntity, RentalPeriod period, BigDecimal totalPrice) {
        if (customerEntity == null) {
            throw new NullPointerException("Client cannot be null.");
        }
        if (carEntity == null) {
            throw new NullPointerException("Car cannot be null.");
        }

        RentalEntity newRentalEntity = new RentalEntity(
                null,
                customerEntity,
                carEntity,
                period.startDate(),
                period.endDate(),
                totalPrice,
                RentalStatus.ACTIVE
        );
        RentalEntity savedEntity = rentalRepository.save(newRentalEntity);
        return rentalMapper.toDomain(savedEntity);
    }

    @Transactional
    public List<Rental> getAllRentals() {
        return rentalRepository.findAll().stream()
                .map(rentalMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional
    public Optional<Rental> findRentalById(Long id) {
        return rentalRepository.findById(id)
                .map(rentalMapper::toDomain);
    }

    @Transactional
    @Modifying
    public Optional<Rental> updateRentalStatus(Long id, RentalStatus newStatus) {
        return rentalRepository.findById(id)
                .map(entity -> {
                    entity.setStatus(newStatus);
                    RentalEntity savedEntity = rentalRepository.save(entity);
                    return rentalMapper.toDomain(savedEntity);
                });
    }

    @Transactional
    @Modifying
    public boolean deleteRental(Long id) {
        return rentalRepository.findById(id)
                .map(entity -> {
                    rentalRepository.delete(entity);
                    return true;
                }).orElse(false);
    }
}