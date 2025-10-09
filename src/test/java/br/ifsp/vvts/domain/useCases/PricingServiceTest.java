package br.ifsp.vvts.domain.useCases;

import br.ifsp.vvts.domain.model.car.Car;
import br.ifsp.vvts.domain.model.car.LicensePlate;
import br.ifsp.vvts.domain.model.rental.RentalPeriod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PricingServiceTest {

    private PricingService pricingService;
    private Car standardCar;
    private final LocalDate MONDAY = LocalDate.of(2025, 10, 6);

    @BeforeEach
    void setUp() {
        pricingService = new PricingService();
        standardCar = new Car(LicensePlate.of("ABC1234"), "Brand", "Model", 100.00);
    }

    @Nested
    @DisplayName("Input Validation Cases")
    class InputValidation {

        @Test
        @DisplayName("Should throw exception when car is null")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldThrowExceptionWhenCarIsNull() {
            var period = new RentalPeriod(MONDAY, MONDAY.plusDays(1));
            assertThatThrownBy(() -> pricingService.calculateTotalPrice(null, period, false))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("The car cannot be null.");
        }

        @Test
        @DisplayName("Should throw exception when rental period is null")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldThrowExceptionWhenPeriodIsNull() {
            assertThatThrownBy(() -> pricingService.calculateTotalPrice(standardCar, null, false))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("The period cannot be null.");
        }

        @ParameterizedTest
        @ValueSource(doubles = {0.00, -50.00})
        @DisplayName("Should throw exception when car base price is zero or negative")
        @Tag("UnitTest")
        @Tag("Functional")
        void shouldThrowExceptionWhenCarPriceIsZeroOrNegative(double invalidPrice) {
            assertThatThrownBy(() -> {
                new Car(LicensePlate.of("ABC1234"), "Brand", "Model", invalidPrice);
            })
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Base price must be positive");
        }
    }

    @Nested
    @DisplayName("Base Price Calculation Cases")
    class BasePriceCalculation {
        @Test
        @DisplayName("Should calculate correct price for a single weekday")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldCalculateCorrectBasePriceForSingleDay() {
            var period = new RentalPeriod(MONDAY, MONDAY.plusDays(1));
            var expectedPrice = "100.00";

            var totalPrice = pricingService.calculateTotalPrice(standardCar, period, false);

            assertThat(totalPrice).isEqualByComparingTo(expectedPrice);
        }

        @Test
        @DisplayName("Should calculate correct price for multiple weekdays")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldCalculateCorrectBasePriceForMultipleDays() {
            var period = new RentalPeriod(MONDAY, MONDAY.plusDays(4));
            var expectedPrice = "400.00";

            var totalPrice = pricingService.calculateTotalPrice(standardCar, period, false);

            assertThat(totalPrice).isEqualByComparingTo(expectedPrice);
        }
    }

    @Nested
    @DisplayName("Surcharge Calculation Cases")
    class SurchargeCalculation {

        @ParameterizedTest
        @CsvSource({
                "'2025-10-10', '2025-10-13', '312.00'",
                "'2025-10-11', '2025-10-13', '212.00'",
                "'2025-10-09', '2025-10-12', '306.00'"
        })
        @DisplayName("Should apply 6% surcharge for each weekend day included in the period")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldApplySurchargeForWeekendDays(String start, String end, String expectedPrice) {
            var period = new RentalPeriod(LocalDate.parse(start), LocalDate.parse(end));

            var totalPrice = pricingService.calculateTotalPrice(standardCar, period, false);

            assertThat(totalPrice).isEqualByComparingTo(expectedPrice);
        }

        @Test
        @DisplayName("Should apply surcharge for a holiday that falls on a weekday")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldApplySurchargeForHolidayOnWeekday() {
            LocalDate holiday = LocalDate.of(2020, 1, 1);
            var period = new RentalPeriod(holiday, holiday.plusDays(1));
            var expectedPrice = "106.00";

            var totalPrice = pricingService.calculateTotalPrice(standardCar, period, false);

            assertThat(totalPrice).isEqualByComparingTo(expectedPrice);
        }

        @Test
        @DisplayName("Should apply surcharge only once for a holiday that falls on a weekend")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldApplySurchargeOnlyOnceForHolidayOnWeekend() {
            LocalDate holidayOnWeekend = LocalDate.of(2025, 10, 12);
            var period = new RentalPeriod(holidayOnWeekend.minusDays(1), holidayOnWeekend.plusDays(1));

            var expectedPrice = "212.00";

            var totalPrice = pricingService.calculateTotalPrice(standardCar, period, false);

            assertThat(totalPrice).isEqualByComparingTo(expectedPrice);
        }
    }

    @Nested
    @DisplayName("Discount Calculation Cases")
    class DiscountCalculation {
        @ParameterizedTest
        @CsvSource({
                "6,  '606.00'",
                "7,  '676.40'",
                "14, '1352.80'",
                "15, '1447.80'",
                "16, '1461.60'"
        })
        @DisplayName("Should apply discount on the final price (after surcharges)")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldApplyCorrectDiscountTiers(long days, String expectedPrice) {
            var period = new RentalPeriod(MONDAY, MONDAY.plusDays(days));

            var totalPrice = pricingService.calculateTotalPrice(standardCar, period, false);

            assertThat(totalPrice).isEqualByComparingTo(expectedPrice);
        }
    }

    @Nested
    @DisplayName("Insurance Fee Calculation Cases")
    class InsuranceCalculation {
        @Test
        @DisplayName("Should apply 10% insurance fee when selected (no other rules)")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldApplyInsuranceFee() {
            var period = new RentalPeriod(MONDAY, MONDAY.plusDays(5));
            var expectedPrice = "550.00";

            var totalPrice = pricingService.calculateTotalPrice(standardCar, period, true);

            assertThat(totalPrice).isEqualByComparingTo(expectedPrice);
        }
    }

    @Nested
    @DisplayName("Combined Calculation Cases")
    class CombinedCalculation {
        @Test
        @DisplayName("Should apply all rules in the correct order (discount last)")
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldCalculateAllRulesInCorrectOrder() {
            var period = new RentalPeriod(LocalDate.of(2025, 10, 9), LocalDate.of(2025, 10, 19));
            var expectedPrice = "1063.81";

            var totalPrice = pricingService.calculateTotalPrice(standardCar, period, true);

            assertThat(totalPrice).isEqualByComparingTo(expectedPrice);
        }
    }
}