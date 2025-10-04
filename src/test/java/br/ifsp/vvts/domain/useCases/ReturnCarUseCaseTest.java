package br.ifsp.vvts.domain.useCases;

import br.ifsp.vvts.domain.model.car.Car;
import br.ifsp.vvts.domain.model.car.LicensePlate;
import br.ifsp.vvts.domain.model.rental.Rental;
import br.ifsp.vvts.domain.model.rental.RentalPeriod;
import br.ifsp.vvts.domain.model.rental.RentalStatus;
import br.ifsp.vvts.infra.persistence.entity.car.CarEntity;
import br.ifsp.vvts.infra.persistence.entity.car.LicensePlateEmbeddable;
import br.ifsp.vvts.infra.persistence.entity.customer.CPFEmbeddable;
import br.ifsp.vvts.infra.persistence.entity.customer.CustomerEntity;
import br.ifsp.vvts.infra.persistence.entity.rental.RentalEntity;
import br.ifsp.vvts.infra.persistence.mapper.RentalMapper;
import br.ifsp.vvts.infra.persistence.repository.RentalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReturnCarUseCaseTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private RentalMapper rentalMapper;

    @InjectMocks
    private ReturnCarUseCase returnCarUseCase;

    private RentalEntity activeRentalEntity;
    private Rental activeRentalDomain;

    private final LocalDate RENTAL_START_DATE = LocalDate.of(2025, 10, 10);

    @BeforeEach
    void setUp() {
        Car standardCar = new Car(LicensePlate.of("ABC1D23"), "Brand", "Model", 100.00);
        var customerEntity = new CustomerEntity(1L, "Test Customer", new CPFEmbeddable("123.456.789-09"));
        var carEntity = new CarEntity(1L, new LicensePlateEmbeddable("ABC1D23"), "Brand", "Model", 100.00);
        var period = new RentalPeriod(RENTAL_START_DATE, RENTAL_START_DATE.plusDays(10));

        activeRentalEntity = new RentalEntity(
                1L, customerEntity, carEntity, period.startDate(), period.endDate(),
                new BigDecimal("1000.00"), RentalStatus.ACTIVE
        );

        activeRentalDomain = new Rental();
        activeRentalDomain.setId(1L);
        activeRentalDomain.setCar(standardCar);
        activeRentalDomain.setPeriod(period);
        activeRentalDomain.setStatus(RentalStatus.ACTIVE);
        activeRentalDomain.setTotalPrice(new BigDecimal("1000.00"));
    }

    @Nested
    @DisplayName("Invalid Return Cases")
    class InvalidReturns {

        @Test
        @DisplayName("Should reject return if rental is already finished")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldRejectReturnForFinishedRental() {
            activeRentalEntity.setStatus(RentalStatus.FINISHED);
            activeRentalDomain.setStatus(RentalStatus.FINISHED);

            when(rentalRepository.findById(1L)).thenReturn(Optional.of(activeRentalEntity));
            when(rentalMapper.toDomain(activeRentalEntity)).thenReturn(activeRentalDomain);

            var request = new ReturnCarUseCase.Request(1L, RENTAL_START_DATE.plusDays(10), false, false);

            assertThatThrownBy(() -> returnCarUseCase.execute(request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Este aluguel já foi encerrado.");

            verify(rentalRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject return if rental is not found")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldRejectReturnForNonExistentRental() {
            when(rentalRepository.findById(99L)).thenReturn(Optional.empty());
            var request = new ReturnCarUseCase.Request(99L, RENTAL_START_DATE.plusDays(10), false, false);

            assertThatThrownBy(() -> returnCarUseCase.execute(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Aluguel inexistente.");

            verify(rentalMapper, never()).toDomain(any());
        }

        @Test
        @DisplayName("Should reject return if the request object is null")
        @Tag("UnitTest")
        @Tag("Functional")
        void shouldRejectReturnIfRequestIsNull() {
            assertThatThrownBy(() -> returnCarUseCase.execute(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("A solicitação de devolução não pode ser nula.");

            verifyNoInteractions(rentalRepository, rentalMapper);
        }

        @Test
        @DisplayName("Should reject return if actual return date is before rental start date")
        @Tag("UnitTest")
        @Tag("Functional")
        void shouldRejectReturnIfDateIsBeforeStartDate() {
            when(rentalRepository.findById(1L)).thenReturn(Optional.of(activeRentalEntity));
            when(rentalMapper.toDomain(activeRentalEntity)).thenReturn(activeRentalDomain);

            LocalDate dateBeforeRentalStarts = RENTAL_START_DATE.minusDays(1);
            var request = new ReturnCarUseCase.Request(1L, dateBeforeRentalStarts, false, false);

            assertThatThrownBy(() -> returnCarUseCase.execute(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("A data de devolução não pode ser anterior à data de início do aluguel.");
        }
    }

    @Nested
    @DisplayName("Standard Return Cases")
    class StandardReturns {
        @Test
        @DisplayName("Should finish contract without fees if returned on time")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldFinishContractOnTime() {
            when(rentalRepository.findById(1L)).thenReturn(Optional.of(activeRentalEntity));
            when(rentalMapper.toDomain(any(RentalEntity.class))).thenReturn(activeRentalDomain);
            when(rentalMapper.toEntity(any(Rental.class))).thenReturn(activeRentalEntity);
            when(rentalRepository.save(any(RentalEntity.class))).thenReturn(activeRentalEntity);

            LocalDate onTimeReturnDate = RENTAL_START_DATE.plusDays(10);
            var request = new ReturnCarUseCase.Request(1L, onTimeReturnDate, false, false);

            Rental result = returnCarUseCase.execute(request);

            assertThat(result.getStatus()).isEqualTo(RentalStatus.FINISHED);
            assertThat(result.getActualReturnDate()).isEqualTo(onTimeReturnDate);
            assertThat(result.getFinalPrice()).isEqualByComparingTo(activeRentalDomain.getTotalPrice());
            verify(rentalRepository).save(any(RentalEntity.class));
        }
    }

    @Nested
    @DisplayName("Return with Penalties and Fees Cases")
    class PenaltiesAndFees {

        @BeforeEach
        void setUpMocks() {
            when(rentalRepository.findById(1L)).thenReturn(Optional.of(activeRentalEntity));
            lenient().when(rentalMapper.toDomain(any(RentalEntity.class))).thenReturn(activeRentalDomain);
            lenient().when(rentalMapper.toEntity(any(Rental.class))).thenReturn(activeRentalEntity);
            lenient().when(rentalRepository.save(any(RentalEntity.class))).thenReturn(activeRentalEntity);
        }

        @Test
        @DisplayName("Should apply penalty for early return")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldApplyPenaltyForEarlyReturn() {
            LocalDate earlyReturnDate = RENTAL_START_DATE.plusDays(7);
            var request = new ReturnCarUseCase.Request(1L, earlyReturnDate, false, false);

            Rental result = returnCarUseCase.execute(request);

            assertThat(result.getFinalPrice()).isEqualByComparingTo("790.00");
            assertThat(result.getStatus()).isEqualTo(RentalStatus.FINISHED);
        }

        @Test
        @DisplayName("Should apply penalty for late return")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldApplyPenaltyForLateReturn() {
            LocalDate lateReturnDate = RENTAL_START_DATE.plusDays(12);
            var request = new ReturnCarUseCase.Request(1L, lateReturnDate, false, false);

            Rental result = returnCarUseCase.execute(request);

            assertThat(result.getFinalPrice()).isEqualByComparingTo("1300.00");
        }

        @Test
        @DisplayName("Should add maintenance fee for damaged car")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldAddMaintenanceFee() {
            LocalDate onTimeReturnDate = RENTAL_START_DATE.plusDays(10);
            var request = new ReturnCarUseCase.Request(1L, onTimeReturnDate, true, false);

            Rental result = returnCarUseCase.execute(request);

            assertThat(result.getFinalPrice()).isEqualByComparingTo("1150.00");
        }

        @Test
        @DisplayName("Should add cleaning fee for dirty car")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldAddCleaningFee() {
            LocalDate onTimeReturnDate = RENTAL_START_DATE.plusDays(10);
            var request = new ReturnCarUseCase.Request(1L, onTimeReturnDate, false, true);

            Rental result = returnCarUseCase.execute(request);

            assertThat(result.getFinalPrice()).isEqualByComparingTo("1100.00");
        }

        @Test
        @DisplayName("Should combine late penalty with maintenance and cleaning fees")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldCombineAllPenaltiesAndFees() {
            LocalDate lateReturnDate = RENTAL_START_DATE.plusDays(12);
            var request = new ReturnCarUseCase.Request(1L, lateReturnDate, true, true);

            Rental result = returnCarUseCase.execute(request);

            assertThat(result.getFinalPrice()).isEqualByComparingTo("1595.00");
        }

        @Test
        @DisplayName("Should combine early return penalty with maintenance and cleaning fees")
        @Tag("UnitTest")
        @Tag("Functional")
        void shouldCombineEarlyReturnWithFees() {
            when(rentalRepository.findById(1L)).thenReturn(Optional.of(activeRentalEntity));
            when(rentalMapper.toDomain(any(RentalEntity.class))).thenReturn(activeRentalDomain);
            when(rentalMapper.toEntity(any(Rental.class))).thenReturn(activeRentalEntity);
            when(rentalRepository.save(any(RentalEntity.class))).thenReturn(activeRentalEntity);

            LocalDate earlyReturnDate = RENTAL_START_DATE.plusDays(7); // 3 dias antes
            var request = new ReturnCarUseCase.Request(1L, earlyReturnDate, true, true);

            Rental result = returnCarUseCase.execute(request);
            assertThat(result.getFinalPrice()).isEqualByComparingTo("1008.50");
        }
    }
}