package br.ifsp.vvts.domain.useCases;

import br.ifsp.vvts.domain.model.car.Car;
import br.ifsp.vvts.domain.model.car.LicensePlate;
import br.ifsp.vvts.infra.persistence.entity.car.CarEntity;
import br.ifsp.vvts.infra.persistence.entity.car.LicensePlateEmbeddable;
import br.ifsp.vvts.infra.persistence.mapper.CarMapper;
import br.ifsp.vvts.infra.persistence.repository.CarRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManageCarUseCaseTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private CarMapper carMapper;

    @InjectMocks
    private ManageCarUseCase manageCarUseCase;

    private final String VALID_LICENSE_PLATE = "ABC1D23";
    private final String INVALID_LICENSE_PLATE = "INVALID_PLATE";
    private final String NON_EXISTENT_LICENSE_PLATE = "XYZ9W87";
    private final LicensePlate VALID_LICENSE_PLATE_OBJECT = LicensePlate.of(VALID_LICENSE_PLATE);
    private final LicensePlateEmbeddable VALID_LICENSE_PLATE_EMBEDDABLE = new LicensePlateEmbeddable(VALID_LICENSE_PLATE);

    private Car car;
    private CarEntity carEntity;

    @BeforeEach
    void setup() {
        car = new Car(VALID_LICENSE_PLATE_OBJECT, "Toyota", "Corolla", 150.0);
        carEntity = new CarEntity(1L, VALID_LICENSE_PLATE_EMBEDDABLE, "Toyota", "Corolla", 150.0);
    }

    @Nested
    @DisplayName("Create Car Cases")
    class CreateCar {

        @Test
        @DisplayName("Should create car successfully with valid data")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldCreateCarSuccessfully() {
            var savedCarEntity = new CarEntity(1L, VALID_LICENSE_PLATE_EMBEDDABLE, "Toyota", "Corolla", 150.0);
            when(carRepository.findByLicensePlate(VALID_LICENSE_PLATE)).thenReturn(Optional.empty());
            when(carMapper.toEntity(any(Car.class))).thenReturn(carEntity);
            when(carRepository.save(carEntity)).thenReturn(savedCarEntity);
            when(carMapper.toDomain(savedCarEntity)).thenReturn(car);

            Car result = manageCarUseCase.createCar(VALID_LICENSE_PLATE, "Toyota", "Corolla", 150.0);

            assertThat(result).isNotNull();
            assertThat(result.licensePlate()).isEqualTo(VALID_LICENSE_PLATE_OBJECT);
            assertThat(result.model()).isEqualTo("Corolla");
            verify(carRepository).save(carEntity);
        }

        @Test
        @DisplayName("Should throw exception when creating car with null license plate")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldThrowExceptionWhenCreatingWithNullLicensePlate() {
            assertThatThrownBy(() -> manageCarUseCase.createCar(null, "Ford", "Mustang", 300.0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when creating car with invalid license plate format")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldThrowExceptionWhenCreatingWithInvalidLicensePlate() {
            assertThatThrownBy(() -> manageCarUseCase.createCar(INVALID_LICENSE_PLATE, "Honda", "Civic", 180.0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Update Car Cases")
    class UpdateCar {

        @Test
        @DisplayName("Should update car successfully")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldUpdateCarSuccessfully() {
            var existingEntity = new CarEntity(1L, VALID_LICENSE_PLATE_EMBEDDABLE, "Old Brand", "Old Model", 100.0);
            var updatedCar = new Car(VALID_LICENSE_PLATE_OBJECT, "New Brand", "New Model", 200.0);
            var savedEntity = new CarEntity(1L, VALID_LICENSE_PLATE_EMBEDDABLE, "New Brand", "New Model", 200.0);

            when(carRepository.findByLicensePlate(VALID_LICENSE_PLATE)).thenReturn(Optional.of(existingEntity));
            when(carRepository.save(any(CarEntity.class))).thenReturn(savedEntity);
            when(carMapper.toDomain(savedEntity)).thenReturn(updatedCar);

            Optional<Car> result = manageCarUseCase.updateCar(VALID_LICENSE_PLATE, "New Brand", "New Model", 200.0);

            assertThat(result).isPresent();
            assertThat(result.get().brand()).isEqualTo("New Brand");
            assertThat(result.get().model()).isEqualTo("New Model");
            verify(carRepository).save(existingEntity);
        }

        @Test
        @DisplayName("Should throw exception when updating with invalid information")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldThrowExceptionWhenUpdatingWithInvalidInfo() {
            assertThatThrownBy(() -> manageCarUseCase.updateCar(VALID_LICENSE_PLATE, "", "Updated Model", 200.0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Car brand cannot be blank");
        }

        @Test
        @DisplayName("Should return empty when updating a non-existent car")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldReturnEmptyWhenUpdatingNonExistentCar() {
            when(carRepository.findByLicensePlate(VALID_LICENSE_PLATE)).thenReturn(Optional.empty());

            Optional<Car> result = manageCarUseCase.updateCar(VALID_LICENSE_PLATE, "Any Brand", "Any Model", 100.0);

            assertThat(result).isNotPresent();
            verify(carRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Delete Car Cases")
    class DeleteCar {

        @Test
        @DisplayName("Should delete car successfully")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldDeleteExistingCarSuccessfully() {
            when(carRepository.findByLicensePlate(VALID_LICENSE_PLATE)).thenReturn(Optional.of(carEntity));
            doNothing().when(carRepository).delete(carEntity);

            boolean result = manageCarUseCase.deleteCar(VALID_LICENSE_PLATE);

            assertThat(result).isTrue();
            verify(carRepository).delete(carEntity);
        }

        @Test
        @DisplayName("Should return false when deleting a non-existent car")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldReturnFalseWhenDeletingNonExistentCar() {
            when(carRepository.findByLicensePlate(NON_EXISTENT_LICENSE_PLATE)).thenReturn(Optional.empty());

            boolean result = manageCarUseCase.deleteCar(NON_EXISTENT_LICENSE_PLATE);

            assertThat(result).isFalse();
            verify(carRepository).findByLicensePlate(NON_EXISTENT_LICENSE_PLATE);
            verify(carRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should return false when deleting a car with an active rental")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldReturnFalseWhenDeletingCarWithActiveRental() {
            boolean result = manageCarUseCase.deleteCar(VALID_LICENSE_PLATE);

            assertThat(result).isFalse();
            verify(carRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("Find Car Cases")
    class FindCar {

        @Test
        @DisplayName("Should find car by license plate successfully")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldFindCarByLicensePlateSuccessfully() {
            when(carRepository.findByLicensePlate(VALID_LICENSE_PLATE)).thenReturn(Optional.of(carEntity));
            when(carMapper.toDomain(carEntity)).thenReturn(car);

            Optional<Car> result = manageCarUseCase.findCarByLicensePlate(VALID_LICENSE_PLATE);

            assertThat(result).isPresent();
            assertThat(result.get().licensePlate()).isEqualTo(VALID_LICENSE_PLATE_OBJECT);
            assertThat(result.get().brand()).isEqualTo("Toyota");
        }

        @Test
        @DisplayName("Should return empty when finding a non-existent car by license plate")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldReturnEmptyWhenFindingNonExistentCar() {
            when(carRepository.findByLicensePlate(NON_EXISTENT_LICENSE_PLATE)).thenReturn(Optional.empty());

            Optional<Car> result = manageCarUseCase.findCarByLicensePlate(NON_EXISTENT_LICENSE_PLATE);

            assertThat(result).isNotPresent();
        }

        @Test
        @DisplayName("Should return all available cars successfully")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldReturnAllCarsSuccessfully() {
            var license1 = LicensePlate.of("ABC1D23");
            var license2 = LicensePlate.of("DEF4E56");
            var license_embeddable_1 = new LicensePlateEmbeddable("ABC1D23");
            var license_embeddable_2 = new LicensePlateEmbeddable("DEF4E56");
            var entity1 = new CarEntity(1L, license_embeddable_1, "Toyota", "Corolla", 150.0);
            var entity2 = new CarEntity(2L, license_embeddable_2, "Honda", "Civic", 180.0);
            var car1 = new Car(license1, "Toyota", "Corolla", 150.0);
            var car2 = new Car(license2, "Honda", "Civic", 180.0);

            when(carRepository.findAll()).thenReturn(List.of(entity1, entity2));
            when(carMapper.toDomain(entity1)).thenReturn(car1);
            when(carMapper.toDomain(entity2)).thenReturn(car2);

            List<Car> result = manageCarUseCase.getAllCars();

            assertThat(result)
                    .isNotNull()
                    .hasSize(2)
                    .containsExactlyInAnyOrder(car1, car2);

            verify(carRepository).findAll();
            verify(carMapper, times(2)).toDomain(any(CarEntity.class));
        }

        @Test
        @DisplayName("Should return an empty list when no cars exist")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldReturnEmptyListWhenNoCarsExist() {
            when(carRepository.findAll()).thenReturn(List.of());

            List<Car> result = manageCarUseCase.getAllCars();

            assertThat(result)
                    .isNotNull()
                    .isEmpty();

            verify(carRepository).findAll();
            verify(carMapper, never()).toDomain(any());
        }
    }

}