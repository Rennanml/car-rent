package br.ifsp.vvts.domain.useCases;

import br.ifsp.vvts.domain.model.car.Car;
import br.ifsp.vvts.domain.model.car.LicensePlate;
import br.ifsp.vvts.domain.model.customer.CPF;
import br.ifsp.vvts.domain.model.customer.Customer;
import br.ifsp.vvts.domain.model.rental.Rental;
import br.ifsp.vvts.domain.model.rental.RentalPeriod;
import br.ifsp.vvts.domain.model.rental.RentalStatus;
import br.ifsp.vvts.exception.CarNotFoundException;
import br.ifsp.vvts.exception.CarUnavailableException;
import br.ifsp.vvts.exception.CustomerNotFoundException;
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


    public CarRentUseCase(CarRepository carRepository, CustomerRepository customerRepository,
                          RentalRepository rentalRepository, PricingService pricingService,
                          CustomerMapper customerMapper, CarMapper carMapper) {
        this.carRepository = carRepository;
        this.customerRepository = customerRepository;
        this.rentalRepository = rentalRepository;
        this.pricingService = pricingService;
        this.customerMapper = customerMapper;
        this.carMapper = carMapper;
    }

    @Transactional
    public Rental execute(String plateValue, String cpfValue, LocalDate startDate, LocalDate endDate, boolean withInsurance) {
        Objects.requireNonNull(plateValue, "Plate is mandatory.");
        Objects.requireNonNull(cpfValue, "CPF is mandatory.");
        LicensePlate licensePlate = LicensePlate.of(plateValue);
        CPF cpf = CPF.of(cpfValue);
        RentalPeriod period = new RentalPeriod(startDate, endDate);

        Customer customer = customerRepository.findByCpfNumber(cpf.unformat())
                .map(customerMapper::toDomain)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found."));

        Car car = carRepository.findByLicensePlate(licensePlate.value())
                .map(carMapper::toDomain)
                .orElseThrow(() -> new CarNotFoundException("Car not found."));

        boolean isUnavailable = rentalRepository.existsByCarLicensePlateAndPeriodOverlaps(car.licensePlate(), period);
        if (isUnavailable) {
            throw new CarUnavailableException("Car unavailable for the requested period.");
        }

        BigDecimal totalPrice = pricingService.calculateTotalPrice(car, period, withInsurance);

        Rental newRental = new Rental();
        newRental.setCustomer(customer);
        newRental.setCar(car);
        newRental.setPeriod(period);
        newRental.setTotalPrice(totalPrice);
        newRental.setStatus(RentalStatus.ACTIVE);

        return rentalRepository.save(newRental);
    }
}