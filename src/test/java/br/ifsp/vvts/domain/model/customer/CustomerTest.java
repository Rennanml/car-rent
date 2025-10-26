package br.ifsp.vvts.domain.model.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Customer Record Tests")
class CustomerTest {

    private CPF validCpf;

    @BeforeEach
    void setUp() {
        validCpf = CPF.of("12345678909");
    }

    @Nested
    @DisplayName("Constructor Validation")
    class ConstructorValidation {

        @Tag("UnitTest")
        @Tag("Structural")
        @DisplayName("Should throw when name is null, empty, or blank")
        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   ", "\t", "\n"})
        void shouldThrowWhenNameIsBlank(String invalidName) {

            assertThatThrownBy(() -> new Customer(invalidName, validCpf))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Customer name cannot be blank");
        }

        @Tag("UnitTest")
        @Tag("Structural")
        @Test
        @DisplayName("Should throw when CPF is null")
        void shouldThrowWhenCpfIsNull() {
            String validName = "Valid Name";

            assertThatThrownBy(() -> new Customer(validName, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Customer CPF cannot be null");
        }
    }

    @Nested
    @DisplayName("State and Behavior")
    class StateAndBehavior {

        @Tag("UnitTest")
        @Tag("Structural")
        @Test
        @DisplayName("Should create instance and get data successfully (Happy Path)")
        void shouldCreateInstanceAndGetData() {
            String name = "John Doe";

            Customer customer = new Customer(name, validCpf);

            assertThat(customer.name()).isEqualTo(name);
            assertThat(customer.cpf()).isEqualTo(validCpf);
        }

        @Tag("UnitTest")
        @Tag("Structural")
        @Test
        @DisplayName("Should return formatted string from toString()")
        void shouldReturnFormattedToString() {
            String name = "Jane Doe";
            Customer customer = new Customer(name, validCpf);

            String expectedString = "Jane Doe - 123.456.789-09";

            assertThat(customer.toString()).isEqualTo(expectedString);
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Contract")
    class EqualsAndHashCode {

        @Tag("UnitTest")
        @Tag("Structural")
        @Test
        @DisplayName("Should be equal when name and CPF are the same")
        void shouldBeEqualForSameData() {
            Customer customer1 = new Customer("John Smith", validCpf);
            Customer customer2 = new Customer("John Smith", validCpf);

            assertThat(customer1).isEqualTo(customer2);
            assertThat(customer1.hashCode()).isEqualTo(customer2.hashCode());
        }

        @Tag("UnitTest")
        @Tag("Structural")
        @Test
        @DisplayName("Should not be equal when name is different")
        void shouldNotBeEqualForDifferentName() {
            Customer customer1 = new Customer("John Smith", validCpf);
            Customer customer2 = new Customer("Jane Smith", validCpf);

            assertThat(customer1).isNotEqualTo(customer2);
        }

        @Tag("UnitTest")
        @Tag("Structural")
        @Test
        @DisplayName("Should not be equal when CPF is different")
        void shouldNotBeEqualForDifferentCpf() {
            CPF otherCpf = CPF.of("79038576064"); // Outro CPF válido
            Customer customer1 = new Customer("John Smith", validCpf);
            Customer customer2 = new Customer("John Smith", otherCpf);

            assertThat(customer1).isNotEqualTo(customer2);
        }
    }
}