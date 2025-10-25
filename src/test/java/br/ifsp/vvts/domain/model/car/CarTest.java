package br.ifsp.vvts.domain.model.car;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@Nested
@DisplayName("Car Record Tests")
class CarTest {

    private final LicensePlate VALID_LICENSE_PLATE = LicensePlate.of("ABC1D34");
    private final String VALID_BRAND = "Fiat";
    private final String VALID_MODEL = "Uno";
    private final double VALID_BASE_PRICE = 15000.0;

    @Test
    @DisplayName("Should construct a Car successfully and verify accessors")
    @Tag("Structural")
    @Tag("UnitTest")
    void shouldConstructCarSuccessfully() {
        Car car = new Car(VALID_LICENSE_PLATE, VALID_BRAND, VALID_MODEL, VALID_BASE_PRICE);

        assertNotNull(car);
        assertEquals(VALID_LICENSE_PLATE, car.licensePlate());
        assertEquals(VALID_BRAND, car.brand());
        assertEquals(VALID_MODEL, car.model());
        assertEquals(VALID_BASE_PRICE, car.basePrice(), 0.001);
    }

    @Test
    @DisplayName("Should return correct String representation")
    @Tag("Structural")
    @Tag("UnitTest")
    void shouldReturnCorrectStringRepresentation() {
        LicensePlate specificPlate = LicensePlate.of("DEF5G67");
        Car car = new Car(specificPlate, "Chevrolet", "Onix", VALID_BASE_PRICE);
        String expected = "Chevrolet Onix - DEF5G67";

        String actual = car.toString();

        assertEquals(expected, actual);
    }

    @Nested
    @DisplayName("Constructor Argument Validations")
    @Tag("Structural")
    @Tag("UnitTest")
    class ConstructorValidationTests {

        @Test
        @DisplayName("Should throw exception when LicensePlate is null")
        @Tag("Structural")
        @Tag("UnitTest")
        void shouldThrowExceptionWhenLicensePlateIsNull() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                new Car(null, VALID_BRAND, VALID_MODEL, VALID_BASE_PRICE);
            });

            assertEquals("License plate cannot be null", exception.getMessage());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t", "\n"})
        @DisplayName("Should throw exception when Brand is null, empty or blank")
        @Tag("Structural")
        @Tag("UnitTest")
        void shouldThrowExceptionWhenBrandIsInvalid(String invalidBrand) {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                new Car(VALID_LICENSE_PLATE, invalidBrand, VALID_MODEL, VALID_BASE_PRICE);
            });

            assertEquals("Brand cannot be blank", exception.getMessage());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t", "\n"})
        @DisplayName("Should throw exception when Model is null, empty or blank")
        @Tag("Structural")
        @Tag("UnitTest")
        void shouldThrowExceptionWhenModelIsInvalid(String invalidModel) {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                new Car(VALID_LICENSE_PLATE, VALID_BRAND, invalidModel, VALID_BASE_PRICE);
            });

            assertEquals("Model cannot be blank", exception.getMessage());
        }

        @ParameterizedTest
        @ValueSource(doubles = {0.0, -1.0, -99.99})
        @DisplayName("Should throw exception when BasePrice is zero or negative")
        @Tag("Structural")
        @Tag("UnitTest")
        void shouldThrowExceptionWhenBasePriceIsNotPositive(double invalidBasePrice) {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                new Car(VALID_LICENSE_PLATE, VALID_BRAND, VALID_MODEL, invalidBasePrice);
            });

            assertEquals("Base price must be positive", exception.getMessage());
        }
    }
}