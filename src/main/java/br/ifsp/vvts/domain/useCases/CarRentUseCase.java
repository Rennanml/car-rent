package br.ifsp.vvts.domain.useCases;

import br.ifsp.vvts.domain.model.car.Car;
import br.ifsp.vvts.domain.model.car.LicensePlate;
import br.ifsp.vvts.domain.model.customer.CPF;
import br.ifsp.vvts.domain.model.rental.Rental;
import br.ifsp.vvts.domain.model.rental.RentalPeriod;
import br.ifsp.vvts.exception.CarNotFoundException;
import br.ifsp.vvts.exception.CarUnavailableException;
import br.ifsp.vvts.exception.CustomerNotFoundException;
import br.ifsp.vvts.infra.persistence.entity.car.CarEntity;
import br.ifsp.vvts.infra.persistence.entity.customer.CustomerEntity;
import br.ifsp.vvts.infra.persistence.mapper.CarMapper;
import br.ifsp.vvts.infra.persistence.mapper.CustomerMapper;
import br.ifsp.vvts.infra.persistence.repository.CarRepository;
import br.ifsp.vvts.infra.persistence.repository.CustomerRepository;
import br.ifsp.vvts.infra.persistence.repository.RentalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Service
public class CarRentUseCase {

    private final CarRepository carRepository;
    private final CustomerRepository customerRepository;
    private final RentalRepository rentalRepository;
    private final PricingService pricingService;
    private final CustomerMapper customerMapper;
    private final CarMapper carMapper;
    private final ManageRentalUseCase manageRentalUseCase;

    public CarRentUseCase(CarRepository carRepository, CustomerRepository customerRepository,
                          RentalRepository rentalRepository, PricingService pricingService,
                          CustomerMapper customerMapper, CarMapper carMapper, ManageRentalUseCase manageRentalUseCase) {
        this.carRepository = carRepository;
        this.customerRepository = customerRepository;
        this.rentalRepository = rentalRepository;
        this.pricingService = pricingService;
        this.customerMapper = customerMapper;
        this.carMapper = carMapper;
        this.manageRentalUseCase = manageRentalUseCase;
    }

    @Transactional
    public Rental execute(String plateValue, String cpfValue, LocalDate startDate, LocalDate endDate, boolean withInsurance) {
        Objects.requireNonNull(plateValue, "Plate is mandatory.");
        Objects.requireNonNull(cpfValue, "CPF is mandatory.");
        LicensePlate licensePlate = LicensePlate.of(plateValue);
        CPF cpf = CPF.of(cpfValue);
        RentalPeriod period = new RentalPeriod(startDate, endDate);

        CustomerEntity customerEntity = customerRepository.findByCpfNumber(cpf.unformat())
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found."));

        CarEntity carEntity = carRepository.findByLicensePlate(licensePlate.value())
                .orElseThrow(() -> new CarNotFoundException("Car not found."));

        Car car = carMapper.toDomain(carEntity);

        boolean isUnavailable = rentalRepository.existsByCarLicensePlateAndPeriodOverlaps(carEntity.getLicensePlate(),
                period.startDate(), period.endDate());
        if (isUnavailable) {
            throw new CarUnavailableException("Car unavailable for the requested period.");
        }

        BigDecimal totalPrice = pricingService.calculateTotalPrice(car, period, withInsurance);

        return manageRentalUseCase.createRental(customerEntity, carEntity, period, totalPrice);
    }
}