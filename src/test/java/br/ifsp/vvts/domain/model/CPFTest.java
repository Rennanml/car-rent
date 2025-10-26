package br.ifsp.vvts.domain.model;

import br.ifsp.vvts.domain.model.customer.CPF;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

class CPFTest {
    @Nested
    @DisplayName("Create CPF Cases")
    public class CreateCPF {
        @ParameterizedTest(name = "Should accept valid CPF: {0}")
        @ValueSource(strings = {
                "12345678909",
                "123.456.789-09"
        })
        @Tag("UnitTest")
        @Tag("TDD")
        public void shouldCreateAValidCPF(String rawCpf) {
            CPF cpf = CPF.of(rawCpf);
            assertThat(cpf.unformat()).isEqualTo("12345678909");
            assertThat(cpf.format()).isEqualTo("123.456.789-09");
        }

        @Test
        @DisplayName("Should create a present Optional<CPF> using tryOf()")
        @Tag("UnitTest")
        @Tag("TDD")
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
        @Tag("UnitTest")
        @Tag("TDD")
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
        @Tag("UnitTest")
        @Tag("TDD")
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
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldCompareEqualCPF() {
            CPF c1 = CPF.of("52998224725");
            CPF c2 = CPF.of("529.982.247-25");

            assertThat(c1).isEqualTo(c2);
            assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
        }

        @Test
        @DisplayName("Should Compare Different CPF")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldCompareDifferentCPF() {
            CPF c1 = CPF.of("52998224725");
            CPF c2 = CPF.of("39053344705");

            assertThat(c1).isNotEqualTo(c2);
        }

        @Test
        @DisplayName("Should Return Formated CPF on toString()")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldReturnFormatedCPFOnToString() {
            CPF cpf = CPF.of("52998224725");

            assertThat(cpf.toString()).isEqualTo("529.982.247-25");
        }

        @Tag("UnitTest")
        @Tag("Structural")
        @Test
        @DisplayName("Should return true when object is compared to itself")
        void shouldReturnTrueForSameInstance() {
            CPF cpf = CPF.of("12345678909");
            assertThat(cpf.equals(cpf)).isTrue();
        }

        @Tag("UnitTest")
        @Tag("Structural")
        @DisplayName("Should return false when compared to null or different type")
        @ParameterizedTest
        @NullSource
        @MethodSource("differentTypesProvider")
        void shouldReturnFalseWhenComparedToNullOrDifferentType(Object otherObject) {
            CPF cpf = CPF.of("12345678909");

            assertThat(cpf.equals(otherObject)).isFalse();
        }

        private static Stream<Object> differentTypesProvider() {
            return Stream.of(
                    new Object(),
                    "12345678909",
                    12345678909L
            );
        }
    }

    @Nested
    @DisplayName("Validate Tests")
    class ValidateTests {
        @Tag("UnitTest")
        @Tag("Structural")
        @DisplayName("Should Return False When CPF is not valid")
        @NullAndEmptySource
        @ParameterizedTest
        @ValueSource(strings = {"12345", "1234567891011"})
        void shouldReturnFalseWhenCpfIsNotValid(String input) {
            assertThat(CPF.isValid(input)).isFalse();
        }

        @Tag("UnitTest")
        @Tag("Structural")
        @DisplayName("Should Return True For Valid CPF Strings")
        @ParameterizedTest
        @ValueSource(strings = {
                "98422104059",
                "34600611039",
                "79038576064",
                "322.625.480-48",
                "963 819 260 74"
        })
        void shouldReturnTrueForValidCpfStrings(String input) {
            assertThat(CPF.isValid(input)).isTrue();
        }

        @Tag("UnitTest")
        @Tag("Structural")
        @DisplayName("Should Return False When First Verifier Digit Is Wrong")
        @ParameterizedTest
        @ValueSource(strings = {
                "12345678919",
                "98765432110"
        })
        void shouldReturnFalseWhenFirstDigitIsWrong(String input) {
            assertThat(CPF.isValid(input)).isFalse();
        }

        @Tag("UnitTest")
        @Tag("Structural")
        @DisplayName("Should Return False When Second Verifier Digit Is Wrong")
        @ParameterizedTest
        @ValueSource(strings = {
                "12345678908",
                "98765432101"
        })
        void shouldReturnFalseWhenSecondDigitIsWrong(String input) {
            assertThat(CPF.isValid(input)).isFalse();
        }

        @Test
        @DisplayName("Should Validate Correctly When First Digit Rest Is Two")
        void shouldValidateCorrectlyWhenFirstDigitRestIsTwo() {
            String cpfValidoComResto2 = "12345671998";

            assertThat(CPF.isValid(cpfValidoComResto2)).isTrue();
        }
    }

    @Nested
    @DisplayName("Hashcode tests")
    class Hashcode {

        @Tag("UnitTest")
        @Tag("Mutation")
        @Test
        @DisplayName("Hash Code Should Be Different When Objects Are Different")
        void hashCodeShouldBeDifferentWhenObjectsAreDifferent() {
            CPF cpf1 = CPF.of("123.456.789-09");
            CPF cpf2 = CPF.of("358.355.130-38");

            assertThat(cpf1).isNotEqualTo(cpf2);

            assertThat(cpf1.hashCode()).isNotEqualTo(cpf2.hashCode());
        }

        @Tag("UnitTest")
        @Tag("Mutation")
        @Test
        @DisplayName("Hash Code Should Be Equal When Objects Are Equal")
        void hashCodeShouldBeEqualWhenObjectsAreEqual() {
            CPF cpf1 = CPF.of("123.456.789-09");
            CPF cpf2 = CPF.of("123.456.789-09");

            assertThat(cpf1).isEqualTo(cpf2);

            assertThat(cpf1.hashCode()).isEqualTo(cpf2.hashCode());
        }
    }
}