package br.ifsp.vvts.domain.useCases;

import br.ifsp.vvts.domain.dto.ReturnCarRequest;
import br.ifsp.vvts.domain.model.car.Car;
import br.ifsp.vvts.domain.model.rental.Rental;
import br.ifsp.vvts.domain.model.rental.RentalStatus;
import br.ifsp.vvts.infra.persistence.entity.rental.RentalEntity;
import br.ifsp.vvts.infra.persistence.mapper.RentalMapper;
import br.ifsp.vvts.infra.persistence.repository.RentalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class ReturnCarUseCase {

    private static final BigDecimal EARLY_RETURN_PENALTY_RATE = new BigDecimal("0.30");
    private static final BigDecimal LATE_RETURN_PENALTY_RATE = new BigDecimal("0.50");
    private static final BigDecimal MAINTENANCE_FEE_RATE = new BigDecimal("0.15");
    private static final BigDecimal CLEANING_FEE_AMOUNT = new BigDecimal("100.00");

    private final RentalRepository rentalRepository;
    private final RentalMapper rentalMapper;

    public ReturnCarUseCase(RentalRepository rentalRepository, RentalMapper rentalMapper) {
        this.rentalRepository = rentalRepository;
        this.rentalMapper = rentalMapper;
    }

    @Transactional
    public Rental execute(ReturnCarRequest request) {
        if (request == null) {
            throw new NullPointerException("A solicitação de devolução não pode ser nula.");
        }

        RentalEntity rentalEntity = rentalRepository.findById(request.rentalId())
                .orElseThrow(() -> new RuntimeException("Aluguel inexistente."));

        Rental rental = rentalMapper.toDomain(rentalEntity);

        if (rental.getStatus() == RentalStatus.FINISHED) {
            throw new IllegalStateException("Este aluguel já foi encerrado.");
        }
        if (request.actualReturnDate().isBefore(rental.getPeriod().startDate())) {
            throw new IllegalArgumentException("A data de devolução não pode ser anterior à data de início do aluguel.");
        }

        BigDecimal finalPrice = calculateFinalPrice(rental, request);

        rentalEntity.setActualReturnDate(request.actualReturnDate());
        rentalEntity.setFinalPrice(finalPrice);
        rentalEntity.setStatus(RentalStatus.FINISHED);

        RentalEntity savedEntity = rentalRepository.save(rentalEntity);

        return rentalMapper.toDomain(savedEntity);
    }

    private BigDecimal calculateFinalPrice(Rental rental, ReturnCarRequest request) {
        Car car = rental.getCar();
        LocalDate expectedReturnDate = rental.getPeriod().endDate();
        LocalDate actualReturnDate = request.actualReturnDate();
        BigDecimal dailyRate = BigDecimal.valueOf(car.basePrice());

        BigDecimal calculatedPrice;

        if (actualReturnDate.isBefore(expectedReturnDate)) {
            long daysUsed = ChronoUnit.DAYS.between(rental.getPeriod().startDate(), actualReturnDate);
            long daysUnused = ChronoUnit.DAYS.between(actualReturnDate, expectedReturnDate);

            BigDecimal priceForDaysUsed = dailyRate.multiply(BigDecimal.valueOf(daysUsed));
            BigDecimal penalty = dailyRate.multiply(BigDecimal.valueOf(daysUnused)).multiply(EARLY_RETURN_PENALTY_RATE);

            calculatedPrice = priceForDaysUsed.add(penalty);

        } else if (actualReturnDate.isAfter(expectedReturnDate)) {
            long lateDays = ChronoUnit.DAYS.between(expectedReturnDate, actualReturnDate);

            BigDecimal extraDaysCost = dailyRate.multiply(BigDecimal.valueOf(lateDays));
            BigDecimal latePenalty = dailyRate.multiply(LATE_RETURN_PENALTY_RATE).multiply(BigDecimal.valueOf(lateDays));

            calculatedPrice = rental.getTotalPrice().add(extraDaysCost).add(latePenalty);

        } else {
            calculatedPrice = rental.getTotalPrice();
        }

        if (request.needsMaintenance()) {
            BigDecimal maintenanceFee = calculatedPrice.multiply(MAINTENANCE_FEE_RATE);
            calculatedPrice = calculatedPrice.add(maintenanceFee);
        }
        if (request.needsCleaning()) {
            calculatedPrice = calculatedPrice.add(CLEANING_FEE_AMOUNT);
        }

        return calculatedPrice.setScale(2, RoundingMode.HALF_UP);
    }
}