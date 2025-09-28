package br.ifsp.vvts.domain.useCases;

import br.ifsp.vvts.domain.model.customer.CPF;
import br.ifsp.vvts.domain.model.customer.Customer;
import br.ifsp.vvts.exception.EntityAlreadyExistsException;
import br.ifsp.vvts.infra.persistence.entity.customer.CPFEmbeddable;
import br.ifsp.vvts.infra.persistence.entity.customer.CustomerEntity;
import br.ifsp.vvts.infra.persistence.mapper.CustomerMapper;
import br.ifsp.vvts.infra.persistence.repository.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
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
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private ManageInventoryUseCase manageInventoryUseCase;

    private final String VALID_CPF_STRING = "123.456.789-09";
    private final String VALID_CPF_UNFORMATTED = "12345678909";
    private final CPF VALID_CPF_OBJECT = CPF.of(VALID_CPF_STRING);
    private final CPFEmbeddable VALID_CPF_EMBEDDABLE = new CPFEmbeddable(VALID_CPF_UNFORMATTED);

    private final String INVALID_CPF_STRING = "123.456.789-10";


    @Nested
    @DisplayName("Create Customer Cases")
    class CreateCustomer {

        @Test
        @DisplayName("Should create customer successfully")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldCreateCustomerSuccessfully() {
            var customer = new Customer("John Doe", VALID_CPF_OBJECT);
            var entity = new CustomerEntity(null, "John Doe", VALID_CPF_EMBEDDABLE);
            var savedEntity = new CustomerEntity(1L, "John Doe", VALID_CPF_EMBEDDABLE);

            when(customerRepository.findByCpfNumber(VALID_CPF_UNFORMATTED)).thenReturn(Optional.empty());
            when(customerMapper.toEntity(any(Customer.class))).thenReturn(entity);
            when(customerRepository.save(entity)).thenReturn(savedEntity);
            when(customerMapper.toDomain(savedEntity)).thenReturn(customer);

            Customer result = manageInventoryUseCase.createCustomer("John Doe", VALID_CPF_STRING);

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("John Doe");
            verify(customerRepository).save(entity);
        }

        @Test
        @DisplayName("Should throw exception when creating customer with invalid CPF")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldThrowExceptionWhenCreatingWithInvalidCPF() {
            assertThatThrownBy(() -> manageInventoryUseCase.createCustomer("John Doe", INVALID_CPF_STRING))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when creating customer with null CPF")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldThrowExceptionWhenCreatingWithNullCPF() {
            assertThatThrownBy(() -> manageInventoryUseCase.createCustomer("John Doe", null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when creating customer with a existing CPF")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldThrowExceptionWhenCreatingWithExistingCPF() {
            var entity = new CustomerEntity(1L, "John Doe", VALID_CPF_EMBEDDABLE);
            when(customerRepository.findByCpfNumber(VALID_CPF_UNFORMATTED)).thenReturn(Optional.of(entity));
            assertThatThrownBy(() -> manageInventoryUseCase.createCustomer("John Doe", VALID_CPF_STRING))
                    .isInstanceOf(EntityAlreadyExistsException.class);
        }
    }

    @Nested
    @DisplayName("Update Customer Cases")
    class UpdateCustomer {

        @Test
        @DisplayName("Should update customer successfully")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldUpdateCustomerSuccessfully() {
            var existingEntity = new CustomerEntity(1L, "John Doe", VALID_CPF_EMBEDDABLE);
            var updatedCustomer = new Customer("John Updated", VALID_CPF_OBJECT);

            when(customerRepository.findByCpfNumber(VALID_CPF_UNFORMATTED)).thenReturn(Optional.of(existingEntity));
            when(customerRepository.save(any(CustomerEntity.class))).thenReturn(existingEntity);
            when(customerMapper.toDomain(existingEntity)).thenReturn(updatedCustomer);

            Optional<Customer> result = manageInventoryUseCase.updateCustomer(VALID_CPF_STRING, "John Updated");

            assertThat(result).isPresent();
            assertThat(result.get().name()).isEqualTo("John Updated");
            verify(customerRepository).save(existingEntity);
        }

        @Test
        @DisplayName("Should throw exception when updating with invalid information")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldThrowExceptionWhenUpdatingWithInvalidInfo() {
            var existingEntity = new CustomerEntity(1L, "John Doe", VALID_CPF_EMBEDDABLE);
            when(customerRepository.findByCpfNumber(VALID_CPF_UNFORMATTED)).thenReturn(Optional.of(existingEntity));

            assertThatThrownBy(() -> manageInventoryUseCase.updateCustomer(VALID_CPF_STRING, ""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Customer name cannot be blank");
        }

        @Test
        @DisplayName("Should return empty when updating a non-existent customer")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldReturnEmptyWhenUpdatingNonExistentCustomer() {
            when(customerRepository.findByCpfNumber(VALID_CPF_UNFORMATTED)).thenReturn(Optional.empty());

            Optional<Customer> result = manageInventoryUseCase.updateCustomer(VALID_CPF_STRING, "Any Name");

            assertThat(result).isNotPresent();
            verify(customerRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Delete Customer Cases")
    class DeleteCustomer {

        @Test
        @DisplayName("Should delete customer successfully")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldDeleteExistingCustomer() {
            var entityToDelete = new CustomerEntity(1L, "John Doe", VALID_CPF_EMBEDDABLE);
            when(customerRepository.findByCpfNumber(VALID_CPF_UNFORMATTED)).thenReturn(Optional.of(entityToDelete));
            doNothing().when(customerRepository).delete(entityToDelete);

            boolean result = manageInventoryUseCase.deleteCustomer(VALID_CPF_STRING);

            assertThat(result).isTrue();
            verify(customerRepository).findByCpfNumber(VALID_CPF_UNFORMATTED);
            verify(customerRepository).delete(entityToDelete);
        }

        @Test
        @DisplayName("Should return false when deleting a non-existent customer")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldReturnFalseWhenDeletingNonExistentCustomer() {
            when(customerRepository.findByCpfNumber(VALID_CPF_UNFORMATTED)).thenReturn(Optional.empty());

            boolean result = manageInventoryUseCase.deleteCustomer(VALID_CPF_UNFORMATTED);

            assertThat(result).isFalse();
            verify(customerRepository).findByCpfNumber(VALID_CPF_UNFORMATTED);
            verify(customerRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("Find Customer Cases")
    class FindCustomer {

        @Test
        @DisplayName("Should find customer by CPF successfully")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldFindExistingCustomerByCpf() {
            var entity = new CustomerEntity(1L, "Jane Doe", VALID_CPF_EMBEDDABLE);
            var customer = new Customer("Jane Doe", VALID_CPF_OBJECT);

            when(customerRepository.findByCpfNumber(VALID_CPF_UNFORMATTED)).thenReturn(Optional.of(entity));
            when(customerMapper.toDomain(entity)).thenReturn(customer);

            Optional<Customer> result = manageInventoryUseCase.findCustomerByCpf(VALID_CPF_STRING);

            assertThat(result).isPresent();
            assertThat(result.get().name()).isEqualTo("Jane Doe");
            assertThat(result.get().cpf().format()).isEqualTo(VALID_CPF_STRING);
        }

        @Test
        @DisplayName("Should return empty when finding a non-existent customer by CPF")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldReturnEmptyWhenFindingNonExistentCustomerByCpf() {
            when(customerRepository.findByCpfNumber(VALID_CPF_UNFORMATTED)).thenReturn(Optional.empty());

            Optional<Customer> result = manageInventoryUseCase.findCustomerByCpf(VALID_CPF_UNFORMATTED);

            assertThat(result).isNotPresent();
        }

        @Test
        @DisplayName("Should return all customers successfully")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldReturnAllCustomersSuccessfully() {
            var entity1 = new CustomerEntity(1L, "John Doe", VALID_CPF_EMBEDDABLE);
            var entity2 = new CustomerEntity(2L, "Jane Doe", new CPFEmbeddable("52081977826"));
            var customer1 = new Customer("John Doe", VALID_CPF_OBJECT);
            var customer2 = new Customer("Jane Doe", CPF.of("520.819.778-26"));

            when(customerRepository.findAll()).thenReturn(List.of(entity1, entity2));
            when(customerMapper.toDomain(entity1)).thenReturn(customer1);
            when(customerMapper.toDomain(entity2)).thenReturn(customer2);

            List<Customer> result = manageInventoryUseCase.getAllCustomers();

            assertThat(result)
                    .isNotNull()
                    .hasSize(2)
                    .containsExactlyInAnyOrder(customer1, customer2);

            verify(customerRepository).findAll();
            verify(customerMapper, times(2)).toDomain(any(CustomerEntity.class));
        }

        @Test
        @DisplayName("Should return an empty list when no customers exist")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldReturnEmptyListWhenNoCustomersExist() {
            when(customerRepository.findAll()).thenReturn(List.of());

            List<Customer> result = manageInventoryUseCase.getAllCustomers();

            assertThat(result)
                    .isNotNull()
                    .isEmpty();

            verify(customerRepository).findAll();
            verify(customerMapper, never()).toDomain(any());
        }
    }
}