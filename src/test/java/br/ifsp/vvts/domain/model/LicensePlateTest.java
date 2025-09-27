package br.ifsp.vvts.domain.model;

import br.ifsp.vvts.domain.model.car.LicensePlate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class LicensePlateTest {

    @Nested
    @DisplayName("License plate validation")
    class LicensePlateValidation {

        @ParameterizedTest(name = "Should accept valid license plate: {0}")
        @ValueSource(strings = {"ABC1234", "BRA2E19", "XYZ9K88"})
        void shouldCreateValidLicensePlates(String plateValue) {
            LicensePlate plate = LicensePlate.of(plateValue);

            assertThat(plate)
                    .isNotNull()
                    .extracting(LicensePlate::value)
                    .isEqualTo(plateValue);
        }

        @ParameterizedTest(name = "Should reject invalid license plate: {0}")
        @ValueSource(strings = {"123ABCD", "A1C2345", "ABCD123", "AB12345", ""})
        void shouldRejectInvalidLicensePlates(String plateValue) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> LicensePlate.of(plateValue))
                    .withMessageContaining("Invalid license plate");
        }

        @Test
        @DisplayName("Should reject null license plate")
        void shouldRejectNullLicensePlate() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> LicensePlate.of(null))
                    .withMessageContaining("Invalid license plate");
        }
    }

    @Nested
    @DisplayName("Equality and hashCode")
    class EqualityAndHashCode {

        @Test
        @DisplayName("Two license plates with the same value should be equal")
        void equalLicensePlates() {
            LicensePlate p1 = LicensePlate.of("ABC1234");
            LicensePlate p2 = LicensePlate.of("ABC1234");

            assertThat(p1).isEqualTo(p2);
            assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
        }

        @Test
        @DisplayName("Two license plates with different values should not be equal")
        void differentLicensePlates() {
            LicensePlate p1 = LicensePlate.of("ABC1234");
            LicensePlate p2 = LicensePlate.of("XYZ9876");

            assertThat(p1).isNotEqualTo(p2);
            assertThat(p1.hashCode()).isNotEqualTo(p2.hashCode());
        }
    }
}
