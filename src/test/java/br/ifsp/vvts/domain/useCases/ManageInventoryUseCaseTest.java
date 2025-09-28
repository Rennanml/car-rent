package br.ifsp.vvts.domain.useCases;

import br.ifsp.vvts.domain.model.costumer.CPF;
import br.ifsp.vvts.domain.model.costumer.Costumer;
import br.ifsp.vvts.exception.EntityAlreadyExistsException;
import br.ifsp.vvts.infra.persistence.entity.costumer.CPFEmbeddable;
import br.ifsp.vvts.infra.persistence.entity.costumer.CostumerEntity;
import br.ifsp.vvts.infra.persistence.mapper.CostumerMapper;
import br.ifsp.vvts.infra.persistence.repository.CostumerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManageInventoryUseCaseTest {

    @Mock
    private CostumerRepository costumerRepository;

    @Mock
    private CostumerMapper costumerMapper;

    @InjectMocks
    private ManageInventoryUseCase manageInventoryUseCase;

    private final String VALID_CPF_STRING = "123.456.789-09";
    private final String VALID_CPF_UNFORMATTED = "12345678909";
    private final CPF VALID_CPF_OBJECT = CPF.of(VALID_CPF_STRING);
    private final CPFEmbeddable VALID_CPF_EMBEDDABLE = new CPFEmbeddable(VALID_CPF_UNFORMATTED);

    private final String INVALID_CPF_STRING = "123.456.789-10";


    @Nested
    @DisplayName("Create Costumer Cases")
    class CreateCostumer {

        @Test
        @DisplayName("Should create costumer successfully")
        void shouldCreateCostumerSuccessfully() {
            var costumer = new Costumer("John Doe", VALID_CPF_OBJECT);
            var entity = new CostumerEntity(null, "John Doe", VALID_CPF_EMBEDDABLE);
            var savedEntity = new CostumerEntity(1L, "John Doe", VALID_CPF_EMBEDDABLE);

            when(costumerRepository.findByCpfNumber(VALID_CPF_UNFORMATTED)).thenReturn(Optional.empty());
            when(costumerMapper.toEntity(any(Costumer.class))).thenReturn(entity);
            when(costumerRepository.save(entity)).thenReturn(savedEntity);
            when(costumerMapper.toDomain(savedEntity)).thenReturn(costumer);

            Costumer result = manageInventoryUseCase.createCostumer("John Doe", VALID_CPF_STRING);

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("John Doe");
            verify(costumerRepository).save(entity);
        }

        @Test
        @DisplayName("Should throw exception when creating costumer with invalid CPF")
        void shouldThrowExceptionWhenCreatingWithInvalidCPF() {
            assertThatThrownBy(() -> manageInventoryUseCase.createCostumer("John Doe", INVALID_CPF_STRING))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when creating costumer with null CPF")
        void shouldThrowExceptionWhenCreatingWithNullCPF() {
            assertThatThrownBy(() -> manageInventoryUseCase.createCostumer("John Doe", null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when creating costumer with a existing CPF")
        void shouldThrowExceptionWhenCreatingWithExistingCPF() {
            var entity = new CostumerEntity(1L, "John Doe", VALID_CPF_EMBEDDABLE);
            when(costumerRepository.findByCpfNumber(VALID_CPF_UNFORMATTED)).thenReturn(Optional.of(entity));
            assertThatThrownBy(() -> manageInventoryUseCase.createCostumer("John Doe", VALID_CPF_STRING))
                    .isInstanceOf(EntityAlreadyExistsException.class);
        }
    }

    @Nested
    @DisplayName("Update Costumer Cases")
    class UpdateCostumer {

        @Test
        @DisplayName("Should update costumer successfully")
        void shouldUpdateCostumerSuccessfully() {
            var existingEntity = new CostumerEntity(1L, "John Doe", VALID_CPF_EMBEDDABLE);
            var updatedCostumer = new Costumer("John Updated", VALID_CPF_OBJECT);

            when(costumerRepository.findByCpfNumber(VALID_CPF_UNFORMATTED)).thenReturn(Optional.of(existingEntity));
            when(costumerRepository.save(any(CostumerEntity.class))).thenReturn(existingEntity);
            when(costumerMapper.toDomain(existingEntity)).thenReturn(updatedCostumer);

            Optional<Costumer> result = manageInventoryUseCase.updateCostumer(VALID_CPF_STRING, "John Updated");

            assertThat(result).isPresent();
            assertThat(result.get().name()).isEqualTo("John Updated");
            verify(costumerRepository).save(existingEntity);
        }

        @Test
        @DisplayName("Should throw exception when updating with invalid information")
        void shouldThrowExceptionWhenUpdatingWithInvalidInfo() {
            var existingEntity = new CostumerEntity(1L, "John Doe", VALID_CPF_EMBEDDABLE);
            when(costumerRepository.findByCpfNumber(VALID_CPF_UNFORMATTED)).thenReturn(Optional.of(existingEntity));

            assertThatThrownBy(() -> manageInventoryUseCase.updateCostumer(VALID_CPF_STRING, ""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Costumer name cannot be blank");
        }

        @Test
        @DisplayName("Should return empty when updating a non-existent costumer")
        void shouldReturnEmptyWhenUpdatingNonExistentCostumer() {
            when(costumerRepository.findByCpfNumber(VALID_CPF_UNFORMATTED)).thenReturn(Optional.empty());

            Optional<Costumer> result = manageInventoryUseCase.updateCostumer(VALID_CPF_STRING, "Any Name");

            assertThat(result).isNotPresent();
            verify(costumerRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Delete Costumer Cases")
    class DeleteCostumer {

        @Test
        @DisplayName("Should delete costumer successfully")
        void shouldDeleteExistingCostumer() {
            var entityToDelete = new CostumerEntity(1L, "John Doe", VALID_CPF_EMBEDDABLE);
            when(costumerRepository.findByCpfNumber(VALID_CPF_UNFORMATTED)).thenReturn(Optional.of(entityToDelete));
            doNothing().when(costumerRepository).delete(entityToDelete);

            boolean result = manageInventoryUseCase.deleteCostumer(VALID_CPF_STRING);

            assertThat(result).isTrue();
            verify(costumerRepository).findByCpfNumber(VALID_CPF_UNFORMATTED);
            verify(costumerRepository).delete(entityToDelete);
        }

        @Test
        @DisplayName("Should return false when deleting a non-existent costumer")
        void shouldReturnFalseWhenDeletingNonExistentCostumer() {
            when(costumerRepository.findByCpfNumber(VALID_CPF_UNFORMATTED)).thenReturn(Optional.empty());

            boolean result = manageInventoryUseCase.deleteCostumer(VALID_CPF_UNFORMATTED);

            assertThat(result).isFalse();
            verify(costumerRepository).findByCpfNumber(VALID_CPF_UNFORMATTED);
            verify(costumerRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("Find Costumer Cases")
    class FindCostumer {

        @Test
        @DisplayName("Should find costumer by CPF successfully")
        void shouldFindExistingCostumerByCpf() {
            var entity = new CostumerEntity(1L, "Jane Doe", VALID_CPF_EMBEDDABLE);
            var costumer = new Costumer("Jane Doe", VALID_CPF_OBJECT);

            when(costumerRepository.findByCpfNumber(VALID_CPF_UNFORMATTED)).thenReturn(Optional.of(entity));
            when(costumerMapper.toDomain(entity)).thenReturn(costumer);

            Optional<Costumer> result = manageInventoryUseCase.findCostumerByCpf(VALID_CPF_STRING);

            assertThat(result).isPresent();
            assertThat(result.get().name()).isEqualTo("Jane Doe");
            assertThat(result.get().cpf().format()).isEqualTo(VALID_CPF_STRING);
        }

        @Test
        @DisplayName("Should return empty when finding a non-existent costumer by CPF")
        void shouldReturnEmptyWhenFindingNonExistentCostumerByCpf() {
            when(costumerRepository.findByCpfNumber(VALID_CPF_UNFORMATTED)).thenReturn(Optional.empty());

            Optional<Costumer> result = manageInventoryUseCase.findCostumerByCpf(VALID_CPF_UNFORMATTED);

            assertThat(result).isNotPresent();
        }

        @Test
        @DisplayName("Should return all costumers successfully")
        void shouldReturnAllCostumersSuccessfully() {
            var entity1 = new CostumerEntity(1L, "John Doe", VALID_CPF_EMBEDDABLE);
            var entity2 = new CostumerEntity(2L, "Jane Doe", new CPFEmbeddable("52081977826"));
            var costumer1 = new Costumer("John Doe", VALID_CPF_OBJECT);
            var costumer2 = new Costumer("Jane Doe", CPF.of("520.819.778-26"));

            when(costumerRepository.findAll()).thenReturn(List.of(entity1, entity2));
            when(costumerMapper.toDomain(entity1)).thenReturn(costumer1);
            when(costumerMapper.toDomain(entity2)).thenReturn(costumer2);

            List<Costumer> result = manageInventoryUseCase.getAllCostumers();

            assertThat(result)
                    .isNotNull()
                    .hasSize(2)
                    .containsExactlyInAnyOrder(costumer1, costumer2);

            verify(costumerRepository).findAll();
            verify(costumerMapper, times(2)).toDomain(any(CostumerEntity.class));
        }

        @Test
        @DisplayName("Should return an empty list when no costumers exist")
        void shouldReturnEmptyListWhenNoCostumersExist() {
            when(costumerRepository.findAll()).thenReturn(List.of());

            List<Costumer> result = manageInventoryUseCase.getAllCostumers();

            assertThat(result)
                    .isNotNull()
                    .isEmpty();

            verify(costumerRepository).findAll();
            verify(costumerMapper, never()).toDomain(any());
        }
    }
}