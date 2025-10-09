package br.ifsp.vvts.domain.useCases;

import br.ifsp.vvts.domain.model.rental.Rental;
import br.ifsp.vvts.domain.model.rental.RentalPeriod;
import br.ifsp.vvts.domain.model.rental.RentalStatus;
import br.ifsp.vvts.infra.persistence.entity.car.CarEntity;
import br.ifsp.vvts.infra.persistence.entity.customer.CustomerEntity;
import br.ifsp.vvts.infra.persistence.entity.rental.RentalEntity;
import br.ifsp.vvts.infra.persistence.mapper.RentalMapper;
import br.ifsp.vvts.infra.persistence.repository.RentalRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ManageRentalUseCase Tests")
class ManageRentalUseCaseTest {

    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private RentalMapper rentalMapper;
    @Mock
    private CustomerEntity customerEntity;
    @Mock
    private CarEntity carEntity;

    @InjectMocks
    private ManageRentalUseCase manageRentalUseCase;

    private RentalEntity rentalEntity;
    private Rental rentalDomain;
    private final Long rentalId = 1L;

    @BeforeEach
    void setUp() {
        rentalEntity = new RentalEntity(rentalId, customerEntity, carEntity, LocalDate.now(), LocalDate.now().plusDays(5), BigDecimal.valueOf(500.0), (RentalStatus.ACTIVE) );
        rentalDomain = new Rental();
        rentalDomain.setId(rentalId);
        rentalDomain.setPeriod(new RentalPeriod(rentalEntity.getStartDate(), rentalEntity.getEndDate()));
        rentalDomain.setTotalPrice(rentalEntity.getTotalPrice());
        rentalDomain.setStatus(RentalStatus.ACTIVE);
    }

    @Nested
    @DisplayName("Create Rental")
    class CreateRental {
        @Test
        @DisplayName("Should successfully create a new rental")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldCreateNewRentalSuccessfully() {
            RentalPeriod period = new RentalPeriod(LocalDate.now(), LocalDate.now().plusDays(5));
            BigDecimal totalPrice = BigDecimal.valueOf(500.0);

            when(rentalRepository.save(any(RentalEntity.class))).thenAnswer(invocation -> {
                RentalEntity entity = invocation.getArgument(0);
                entity.setId(1L);
                return entity;
            });
            when(rentalMapper.toDomain(any(RentalEntity.class))).thenReturn(rentalDomain);

            Rental result = manageRentalUseCase.createRental(customerEntity, carEntity, period, totalPrice);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTotalPrice()).isEqualByComparingTo(totalPrice);
            assertThat(result.getStatus()).isEqualTo(RentalStatus.ACTIVE);
            verify(rentalRepository, times(1)).save(any(RentalEntity.class));
            verify(rentalMapper, times(1)).toDomain(any(RentalEntity.class));
        }

        @Test
        @DisplayName("Should throw exception when creating a rental with a null customer")
        @Tag("UnitTest")
        @Tag("Functional")
        void shouldThrowExceptionWhenCustomerIsNull() {
            RentalPeriod period = new RentalPeriod(LocalDate.now(), LocalDate.now().plusDays(5));
            BigDecimal totalPrice = BigDecimal.valueOf(500.0);

            assertThatThrownBy(() -> manageRentalUseCase.createRental(null, carEntity, period, totalPrice))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Client cannot be null.");

            verify(rentalRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when creating a rental with a null car")
        @Tag("UnitTest")
        @Tag("Functional")
        void shouldThrowExceptionWhenCarIsNull() {
            RentalPeriod period = new RentalPeriod(LocalDate.now(), LocalDate.now().plusDays(5));
            BigDecimal totalPrice = BigDecimal.valueOf(500.0);

            assertThatThrownBy(() -> manageRentalUseCase.createRental(customerEntity, null, period, totalPrice))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Car cannot be null.");

            verify(rentalRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("List All Rentals")
    class ListAllRentals {
        @Test
        @DisplayName("Should return a list of rentals when the repository has data")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldReturnListWhenRepositoryHasData() {
            when(rentalRepository.findAll()).thenReturn(Collections.singletonList(rentalEntity));
            when(rentalMapper.toDomain(rentalEntity)).thenReturn(rentalDomain);

            List<Rental> result = manageRentalUseCase.getAllRentals();

            assertThat(result).hasSize(1);
            assertThat(result.getFirst()).isEqualTo(rentalDomain);
            verify(rentalRepository, times(1)).findAll();
            verify(rentalMapper, times(1)).toDomain(rentalEntity);
        }

        @Test
        @DisplayName("Should return an empty list when the repository is empty")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldReturnEmptyListWhenRepositoryIsEmpty() {
            when(rentalRepository.findAll()).thenReturn(Collections.emptyList());

            List<Rental> result = manageRentalUseCase.getAllRentals();

            assertThat(result).isEmpty();
            verify(rentalRepository, times(1)).findAll();
            verifyNoInteractions(rentalMapper);
        }
    }

    @Nested
    @DisplayName("Find Rental by ID")
    class FindRentalById {
        @Test
        @DisplayName("Should return a rental when ID exists")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldReturnRentalWhenIdExists() {
            when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rentalEntity));
            when(rentalMapper.toDomain(rentalEntity)).thenReturn(rentalDomain);

            Optional<Rental> result = manageRentalUseCase.findRentalById(rentalId);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(rentalDomain);
            verify(rentalRepository, times(1)).findById(rentalId);
            verify(rentalMapper, times(1)).toDomain(rentalEntity);
        }

        @Test
        @DisplayName("Should return an empty optional when ID does not exist")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldReturnEmptyOptionalWhenIdDoesNotExist() {
            when(rentalRepository.findById(rentalId)).thenReturn(Optional.empty());

            Optional<Rental> result = manageRentalUseCase.findRentalById(rentalId);

            assertThat(result).isEmpty();
            verify(rentalRepository, times(1)).findById(rentalId);
            verifyNoInteractions(rentalMapper);
        }
    }

    @Nested
    @DisplayName("Update Rental Status")
    class UpdateRentalStatus {
        @Test
        @DisplayName("Should update the status and return the updated rental when ID exists")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldUpdateStatusWhenIdExists() {
            RentalStatus newStatus = RentalStatus.FINISHED;
            when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rentalEntity));
            when(rentalRepository.save(any(RentalEntity.class))).thenReturn(rentalEntity);
            when(rentalMapper.toDomain(any(RentalEntity.class))).thenReturn(rentalDomain);

            Optional<Rental> result = manageRentalUseCase.updateRentalStatus(rentalId, newStatus);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(rentalDomain);
            verify(rentalRepository, times(1)).findById(rentalId);
            verify(rentalRepository, times(1)).save(rentalEntity);
            verify(rentalMapper, times(1)).toDomain(any(RentalEntity.class));
            assertThat(rentalEntity.getStatus()).isEqualTo(newStatus);
        }

        @Test
        @DisplayName("Should return an empty optional when trying to update a non-existent rental")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldReturnEmptyOptionalWhenUpdatingNonExistentRental() {
            when(rentalRepository.findById(rentalId)).thenReturn(Optional.empty());

            Optional<Rental> result = manageRentalUseCase.updateRentalStatus(rentalId, RentalStatus.CANCELED);

            assertThat(result).isEmpty();
            verify(rentalRepository, times(1)).findById(rentalId);
            verify(rentalRepository, never()).save(any());
            verifyNoInteractions(rentalMapper);
        }
    }

    @Nested
    @DisplayName("Delete Rental")
    class DeleteRental {
        @Test
        @DisplayName("Should delete the rental and return true when ID exists")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldDeleteRentalWhenIdExists() {
            when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rentalEntity));
            doNothing().when(rentalRepository).delete(rentalEntity);

            boolean result = manageRentalUseCase.deleteRental(rentalId);

            assertThat(result).isTrue();
            verify(rentalRepository, times(1)).findById(rentalId);
            verify(rentalRepository, times(1)).delete(rentalEntity);
        }

        @Test
        @DisplayName("Should return false when trying to delete a non-existent rental")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldReturnFalseWhenDeletingNonExistentRental() {
            when(rentalRepository.findById(rentalId)).thenReturn(Optional.empty());

            boolean result = manageRentalUseCase.deleteRental(rentalId);

            assertThat(result).isFalse();
            verify(rentalRepository, times(1)).findById(rentalId);
            verify(rentalRepository, never()).delete(any());
        }
    }
}