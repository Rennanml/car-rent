package br.ifsp.vvts.domain.useCases;

import br.ifsp.vvts.domain.model.car.Car;
import br.ifsp.vvts.domain.model.car.LicensePlate;
import br.ifsp.vvts.domain.model.customer.CPF;
import br.ifsp.vvts.domain.model.customer.Customer;
import br.ifsp.vvts.domain.model.rental.Rental;
import br.ifsp.vvts.domain.model.rental.RentalPeriod;
import br.ifsp.vvts.exception.CarNotFoundException;
import br.ifsp.vvts.exception.CarUnavailableException;
import br.ifsp.vvts.exception.CustomerNotFoundException;
import br.ifsp.vvts.infra.persistence.entity.car.CarEntity;
import br.ifsp.vvts.infra.persistence.entity.car.LicensePlateEmbeddable;
import br.ifsp.vvts.infra.persistence.entity.customer.CPFEmbeddable;
import br.ifsp.vvts.infra.persistence.entity.customer.CustomerEntity;
import br.ifsp.vvts.infra.persistence.mapper.CarMapper;
import br.ifsp.vvts.infra.persistence.mapper.CustomerMapper;
import br.ifsp.vvts.infra.persistence.repository.CarRepository;
import br.ifsp.vvts.infra.persistence.repository.CustomerRepository;
import br.ifsp.vvts.infra.persistence.repository.RentalRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CarRentUseCase Tests")
class CarRentUseCaseTest {

    @Mock
    private CarRepository carRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private PricingService pricingService;
    @Mock
    private CustomerMapper customerMapper;
    @Mock
    private CarMapper carMapper;

    @InjectMocks
    private CarRentUseCase carRentUseCase;

    private final String validCpf = "515.891.860-01";
    private final String validPlate = "ABC1D23";
    private final LocalDate today = LocalDate.now();
    private final LocalDate inFiveDays = today.plusDays(5);
    private final CustomerEntity existingCustomer = new CustomerEntity(1L, "John Doe", new CPFEmbeddable(validCpf));
    private final CarEntity existingCar = new CarEntity(1L, new LicensePlateEmbeddable(validPlate), "Nissan", "March", 100);
    private final Customer customerDomain = new Customer("John Doe", CPF.of(validCpf));
    private final Car carDomain = new Car(LicensePlate.of(validPlate), "Nissan", "March", 100);

    @Nested
    @DisplayName("Input Validation Cases")
    class InputValidation {

        @Test
        @DisplayName("Should reject when license plate is null")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldThrowExceptionWhenPlateIsNull() {
            assertThatThrownBy(() -> carRentUseCase.execute(null, validCpf, today, inFiveDays, false))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Plate is mandatory.");
            verifyNoInteractions(carRepository, customerRepository, rentalRepository, pricingService, customerMapper, carMapper);
        }

        @Test
        @DisplayName("Should reject when license plate has invalid format")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldThrowExceptionWhenPlateIsInvalid() {
            String invalidPlate = "111ABC";
            assertThatThrownBy(() -> carRentUseCase.execute(invalidPlate, validCpf, today, inFiveDays, false))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid license plate: 111ABC");
            verifyNoInteractions(carRepository, customerRepository, rentalRepository, pricingService, customerMapper, carMapper);
        }

        @Test
        @DisplayName("Should reject when CPF is null")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldThrowExceptionWhenCpfIsNull() {
            assertThatThrownBy(() -> carRentUseCase.execute(validPlate, null, today, inFiveDays, false))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("CPF is mandatory.");
            verifyNoInteractions(carRepository, customerRepository, rentalRepository, pricingService, customerMapper, carMapper);
        }

        @Test
        @DisplayName("Should reject when CPF is invalid")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldThrowExceptionWhenCpfIsInvalid() {
            String invalidCpf = "123.456.789-10";
            assertThatThrownBy(() -> carRentUseCase.execute(validPlate, invalidCpf, today, inFiveDays, false))
                    .isInstanceOf(IllegalArgumentException.class);
            verifyNoInteractions(carRepository, customerRepository, rentalRepository, pricingService, customerMapper, carMapper);
        }

        @Test
        @DisplayName("Should reject when rental period is invalid (end before start)")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldThrowExceptionWhenPeriodIsInvalid() {
            assertThatThrownBy(() -> carRentUseCase.execute(validPlate, validCpf, inFiveDays, today, false))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid rental period: End date must be after start date.");
            verifyNoInteractions(carRepository, customerRepository, rentalRepository, pricingService, customerMapper, carMapper);
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("Should throw exception when start date is null")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldThrowExceptionWhenStartDateIsNull(LocalDate nullStartDate) {
            assertThatThrownBy(() -> carRentUseCase.execute(validPlate, validCpf, nullStartDate, today, false))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Mandatory start date.");
            verifyNoInteractions(carRepository, customerRepository, rentalRepository, pricingService, customerMapper, carMapper);
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("Should throw exception when end date is null")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldThrowExceptionWhenEndDateIsNull(LocalDate nullEndDate) {
            assertThatThrownBy(() -> carRentUseCase.execute(validPlate, validCpf, today, nullEndDate, false))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Mandatory end date.");
            verifyNoInteractions(carRepository, customerRepository, rentalRepository, pricingService, customerMapper, carMapper);
        }

        @Test
        @DisplayName("Should reject when start and end dates are equal")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldThrowExceptionWhenStartAndEndDateAreEqual() {
            assertThatThrownBy(() -> carRentUseCase.execute(validPlate, validCpf, today, today, false))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid rental period: End date must be after start date.");
            verifyNoInteractions(carRepository, customerRepository, rentalRepository, pricingService, customerMapper, carMapper);
        }

        @Test
        @DisplayName("Should reject when rental period is too long (over 60 days)")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldThrowExceptionWhenPeriodIsTooLong() {
            LocalDate endIn61Days = today.plusDays(61);
            assertThatThrownBy(() -> carRentUseCase.execute(validPlate, validCpf, today, endIn61Days, false))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid rental period: The interval cannot be longer than 60 days.");
            verifyNoInteractions(carRepository, customerRepository, rentalRepository, pricingService, customerMapper, carMapper);
        }
    }

    @Nested
    @DisplayName("Business Rule and Existence Cases")
    class BusinessRules {
        @Test
        @DisplayName("Should reject when customer does not exist")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldThrowExceptionWhenCustomerNotFound() {
            when(customerRepository.findByCpfNumber(CPF.of(validCpf).unformat())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> carRentUseCase.execute(validPlate, validCpf, today, inFiveDays, false))
                    .isInstanceOf(CustomerNotFoundException.class)
                    .hasMessage("Customer not found.");

            verify(carRepository, never()).findByLicensePlate(anyString());
            verify(rentalRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject when car does not exist")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldThrowExceptionWhenCarNotFound() {
            when(customerRepository.findByCpfNumber(CPF.of(validCpf).unformat())).thenReturn(Optional.of(existingCustomer));
            when(customerMapper.toDomain(existingCustomer)).thenReturn(customerDomain);
            when(carRepository.findByLicensePlate(LicensePlate.of(validPlate).value())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> carRentUseCase.execute(validPlate, validCpf, today, inFiveDays, false))
                    .isInstanceOf(CarNotFoundException.class)
                    .hasMessage("Car not found.");

            verify(rentalRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject when car is already rented for the period")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldThrowExceptionWhenCarIsUnavailable() {
            when(customerRepository.findByCpfNumber(CPF.of(validCpf).unformat())).thenReturn(Optional.of(existingCustomer));
            when(customerMapper.toDomain(existingCustomer)).thenReturn(customerDomain);
            when(carRepository.findByLicensePlate(LicensePlate.of(validPlate).value())).thenReturn(Optional.of(existingCar));
            when(carMapper.toDomain(existingCar)).thenReturn(carDomain);
            when(rentalRepository.existsByCarLicensePlateAndPeriodOverlaps(any(), any())).thenReturn(true);

            assertThatThrownBy(() -> carRentUseCase.execute(validPlate, validCpf, today, inFiveDays, false))
                    .isInstanceOf(CarUnavailableException.class)
                    .hasMessage("Car unavailable for the requested period.");

            verify(rentalRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Successful Rental Cases")
    class Success {

        @Test
        @DisplayName("Should register rental with valid data")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldRegisterRentalSuccessfully() {
            when(customerRepository.findByCpfNumber(CPF.of(validCpf).unformat())).thenReturn(Optional.of(existingCustomer));
            when(customerMapper.toDomain(existingCustomer)).thenReturn(customerDomain);
            when(carRepository.findByLicensePlate(LicensePlate.of(validPlate).value())).thenReturn(Optional.of(existingCar));
            when(carMapper.toDomain(existingCar)).thenReturn(carDomain);
            when(rentalRepository.existsByCarLicensePlateAndPeriodOverlaps(any(), any())).thenReturn(false);
            when(pricingService.calculateTotalPrice(any(Car.class), any(RentalPeriod.class), eq(false))).thenReturn(BigDecimal.TEN);
            when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Rental result = carRentUseCase.execute(validPlate, validCpf, today, inFiveDays, false);

            assertThat(result).isNotNull();
            assertThat(result.getCar().licensePlate().value()).isEqualTo(validPlate);
            assertThat(result.getCustomer().cpf().toString()).isEqualTo(validCpf);
            assertThat(result.getTotalPrice()).isEqualByComparingTo(BigDecimal.TEN);
            verify(rentalRepository, times(1)).save(any(Rental.class));
        }

        @Test
        @DisplayName("Should register rental without discounts or surcharges")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldRegisterRentalWithoutDiscountsOrSurcharges() {
            when(customerRepository.findByCpfNumber(CPF.of(validCpf).unformat())).thenReturn(Optional.of(existingCustomer));
            when(customerMapper.toDomain(existingCustomer)).thenReturn(customerDomain);
            when(carRepository.findByLicensePlate(LicensePlate.of(validPlate).value())).thenReturn(Optional.of(existingCar));
            when(carMapper.toDomain(existingCar)).thenReturn(carDomain);
            when(rentalRepository.existsByCarLicensePlateAndPeriodOverlaps(any(), any())).thenReturn(false);
            when(pricingService.calculateTotalPrice(any(Car.class), any(RentalPeriod.class), eq(false)))
                    .thenReturn(BigDecimal.valueOf(500.0));
            when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Rental result = carRentUseCase.execute(validPlate, validCpf, today, today.plusDays(5), false);

            assertThat(result).isNotNull();
            assertThat(result.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(500.0));
            verify(rentalRepository, times(1)).save(any(Rental.class));
        }

        @Test
        @DisplayName("Should apply 5% discount for rentals between 7 and 14 days")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldApplyDiscountForPeriodBetween7And14Days() {
            LocalDate endDate = today.plusDays(8);
            when(customerRepository.findByCpfNumber(CPF.of(validCpf).unformat())).thenReturn(Optional.of(existingCustomer));
            when(customerMapper.toDomain(existingCustomer)).thenReturn(customerDomain);
            when(carRepository.findByLicensePlate(LicensePlate.of(validPlate).value())).thenReturn(Optional.of(existingCar));
            when(carMapper.toDomain(existingCar)).thenReturn(carDomain);
            when(rentalRepository.existsByCarLicensePlateAndPeriodOverlaps(any(), any())).thenReturn(false);
            when(pricingService.calculateTotalPrice(any(Car.class), any(RentalPeriod.class), eq(false)))
                    .thenReturn(BigDecimal.valueOf(760.0));
            when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Rental result = carRentUseCase.execute(validPlate, validCpf, today, endDate, false);

            assertThat(result).isNotNull();
            assertThat(result.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(760.0));
            verify(rentalRepository, times(1)).save(any(Rental.class));
        }

        @Test
        @DisplayName("Should apply 10% discount for rentals over 15 days")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldApplyDiscountForPeriodOver15Days() {
            LocalDate endDate = today.plusDays(15);
            when(customerRepository.findByCpfNumber(CPF.of(validCpf).unformat())).thenReturn(Optional.of(existingCustomer));
            when(customerMapper.toDomain(existingCustomer)).thenReturn(customerDomain);
            when(carRepository.findByLicensePlate(LicensePlate.of(validPlate).value())).thenReturn(Optional.of(existingCar));
            when(carMapper.toDomain(existingCar)).thenReturn(carDomain);
            when(rentalRepository.existsByCarLicensePlateAndPeriodOverlaps(any(), any())).thenReturn(false);
            when(pricingService.calculateTotalPrice(any(Car.class), any(RentalPeriod.class), eq(false)))
                    .thenReturn(BigDecimal.valueOf(1350.0));
            when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Rental result = carRentUseCase.execute(validPlate, validCpf, today, endDate, false);

            assertThat(result).isNotNull();
            assertThat(result.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(1350.0));
            verify(rentalRepository, times(1)).save(any(Rental.class));
        }

        @Test
        @DisplayName("Should apply surcharge for weekend or holiday")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldApplySurchargeForWeekendOrHoliday() {
            LocalDate startDate = LocalDate.of(2025, 10, 3);
            LocalDate endDate = LocalDate.of(2025, 10, 5);
            when(customerRepository.findByCpfNumber(CPF.of(validCpf).unformat())).thenReturn(Optional.of(existingCustomer));
            when(customerMapper.toDomain(existingCustomer)).thenReturn(customerDomain);
            when(carRepository.findByLicensePlate(LicensePlate.of(validPlate).value())).thenReturn(Optional.of(existingCar));
            when(carMapper.toDomain(existingCar)).thenReturn(carDomain);
            when(rentalRepository.existsByCarLicensePlateAndPeriodOverlaps(any(), any())).thenReturn(false);
            when(pricingService.calculateTotalPrice(any(Car.class), any(RentalPeriod.class), eq(false)))
                    .thenReturn(BigDecimal.valueOf(312.0));
            when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Rental result = carRentUseCase.execute(validPlate, validCpf, startDate, endDate, false);

            assertThat(result).isNotNull();
            assertThat(result.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(312.0));
            verify(rentalRepository, times(1)).save(any(Rental.class));
        }

        @Test
        @DisplayName("Should apply insurance surcharge when requested")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldApplyInsuranceSurchargeWhenRequested() {
            LocalDate endDate = today.plusDays(1);
            when(customerRepository.findByCpfNumber(CPF.of(validCpf).unformat())).thenReturn(Optional.of(existingCustomer));
            when(customerMapper.toDomain(existingCustomer)).thenReturn(customerDomain);
            when(carRepository.findByLicensePlate(LicensePlate.of(validPlate).value())).thenReturn(Optional.of(existingCar));
            when(carMapper.toDomain(existingCar)).thenReturn(carDomain);
            when(rentalRepository.existsByCarLicensePlateAndPeriodOverlaps(any(), any())).thenReturn(false);
            when(pricingService.calculateTotalPrice(any(Car.class), any(RentalPeriod.class), eq(true)))
                    .thenReturn(BigDecimal.valueOf(220.0));
            when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Rental result = carRentUseCase.execute(validPlate, validCpf, today, endDate, true);

            assertThat(result).isNotNull();
            assertThat(result.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(220.0));
            verify(rentalRepository, times(1)).save(any(Rental.class));
        }
    }
}