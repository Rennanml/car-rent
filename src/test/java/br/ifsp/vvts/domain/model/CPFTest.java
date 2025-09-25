package br.ifsp.vvts.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class CPFTest {
    @Nested
    @DisplayName("Create CPF Cases")
    public class CreateCPF {
        @ParameterizedTest(name = "Should accept valid CPF: {0}")
        @ValueSource(strings = {
                "12345678909",
                "123.456.789-09"
        })
        public void shouldCreateAValidCPF(String rawCpf) {
            CPF cpf = CPF.of(rawCpf);
            assertThat(cpf.unformat()).isEqualTo("12345678909");
            assertThat(cpf.format()).isEqualTo("123.456.789-09");
        }

        @Test
        @DisplayName("Should create a present Optional<CPF> using tryOf()")
        void shouldCreateAValidCPFUsingTryOf() {
            Optional<CPF> maybeCpf = CPF.tryOf("12345678909");
            assertThat(maybeCpf)
                    .isPresent()
                    .get()
                    .extracting(CPF::unformat)
                    .isEqualTo("12345678909");
        }
    }

    @Nested
    @DisplayName("Invalid CPF Creation Cases")
    class InvalidCpfCreation {

        @ParameterizedTest(name = "Should reject invalid CPF: {0}")
        @ValueSource(strings = {
                "12345678900",
                "11111111111",
                "00000000000",
                "5299822472",
                "5299822472599",
                "529.982.247-2X"
        })
        void shouldThrowIllegalArgumentExceptionWhenCPFIsInvalid(String rawCpf) {
            assertThatIllegalArgumentException().isThrownBy(() -> CPF.of(rawCpf));
        }

        @ParameterizedTest(name = "Should return Optional.empty() for invalid CPF: {0}")
        @ValueSource(strings = {
                "12345678900",
                "11111111111",
                "00000000000",
                "5299822472",
                "5299822472599",
                "529.982.247-2X"
        })
        void shouldReturnOptionEmptyForInvalidCPF(String rawCpf) {
            Optional<CPF> maybeCpf = CPF.tryOf(rawCpf);
            assertThat(maybeCpf).isEmpty();
        }
    }

    @Nested
    @DisplayName("Equals and Transformations Cases")
    class EqualityAndTransformations {

        @Test
        @DisplayName("Two identical CPFs (with and without formatting) should be equal")
        void shouldCompareEqualCPF() {
            CPF c1 = CPF.of("52998224725");
            CPF c2 = CPF.of("529.982.247-25");

            assertThat(c1).isEqualTo(c2);
            assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
        }

        @Test
        @DisplayName("Should Compare Different CPF")
        void shouldCompareDifferentCPF() {
            CPF c1 = CPF.of("52998224725");
            CPF c2 = CPF.of("39053344705");

            assertThat(c1).isNotEqualTo(c2);
        }

        @Test
        @DisplayName("Should Return Formated CPF on toString()")
        void shouldReturnFormatedCPFOnToString() {
            CPF cpf = CPF.of("52998224725");

            assertThat(cpf.toString()).isEqualTo("529.982.247-25");
        }
    }
}