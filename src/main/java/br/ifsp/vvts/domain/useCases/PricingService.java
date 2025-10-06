package br.ifsp.vvts.domain.useCases;

import br.ifsp.vvts.domain.model.car.Car;
import br.ifsp.vvts.domain.model.rental.RentalPeriod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PricingService {

    private static final BigDecimal WEEKEND_HOLIDAY_SURCHARGE = new BigDecimal("0.06");
    private static final BigDecimal INSURANCE_FEE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_7_TO_14_DAYS = new BigDecimal("0.05");
    private static final BigDecimal DISCOUNT_ABOVE_15_DAYS = new BigDecimal("0.10");

    private static final List<String> HOLIDAY_STRINGS = List.of(
            // 2020
            "2020-01-01", "2020-02-24", "2020-02-25", "2020-04-10", "2020-04-21", "2020-05-01", "2020-06-11", "2020-09-07", "2020-10-12", "2020-11-02", "2020-11-15", "2020-12-25",
            // 2021
            "2021-01-01", "2021-02-15", "2021-02-16", "2021-04-02", "2021-04-21", "2021-05-01", "2021-06-03", "2021-09-07", "2021-10-12", "2021-11-02", "2021-11-15", "2021-12-25",
            // 2022
            "2022-01-01", "2022-02-28", "2022-03-01", "2022-04-15", "2022-04-21", "2022-05-01", "2022-06-16", "2022-09-07", "2022-10-12", "2022-11-02", "2022-11-15", "2022-12-25",
            // 2023
            "2023-01-01", "2023-02-20", "2023-02-21", "2023-04-07", "2023-04-21", "2023-05-01", "2023-06-08", "2023-09-07", "2023-10-12", "2023-11-02", "2023-11-15", "2023-12-25",
            // 2024
            "2024-01-01", "2024-02-12", "2024-02-13", "2024-03-29", "2024-04-21", "2024-05-01", "2024-05-30", "2024-09-07", "2024-10-12", "2024-11-02", "2024-11-15", "2024-12-25",
            // 2025
            "2025-01-01", "2025-03-03", "2025-03-04", "2025-04-18", "2025-04-21", "2025-05-01", "2025-06-19", "2025-09-07", "2025-10-12", "2025-11-02", "2025-11-15", "2025-12-25",
            // 2026
            "2026-01-01", "2026-02-16", "2026-02-17", "2026-04-03", "2026-04-21", "2026-05-01", "2026-06-04", "2026-09-07", "2026-10-12", "2026-11-02", "2026-11-15", "2026-12-25",
            // 2027
            "2027-01-01", "2027-02-08", "2027-02-09", "2027-03-26", "2027-04-21", "2027-05-01", "2027-05-27", "2027-09-07", "2027-10-12", "2027-11-02", "2027-11-15", "2027-12-25",
            // 2028
            "2028-01-01", "2028-02-28", "2028-02-29", "2028-04-14", "2028-04-21", "2028-05-01", "2028-06-15", "2028-09-07", "2028-10-12", "2028-11-02", "2028-11-15", "2028-12-25",
            // 2029
            "2029-01-01", "2029-02-12", "2029-02-13", "2029-03-30", "2029-04-21", "2029-05-01", "2029-05-31", "2029-09-07", "2029-10-12", "2029-11-02", "2029-11-15", "2029-12-25",
            // 2030
            "2030-01-01", "2030-03-04", "2030-03-05", "2030-04-19", "2030-04-21", "2030-05-01", "2030-06-20", "2030-09-07", "2030-10-12", "2030-11-02", "2030-11-15", "2030-12-25"
    );

    private static final Set<LocalDate> holidays = new HashSet<>();

    static {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (String dateString : HOLIDAY_STRINGS) {
            holidays.add(LocalDate.parse(dateString.trim(), formatter));
        }
    }

    private boolean isWeekendOrHoliday(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY || holidays.contains(date);
    }

    public BigDecimal calculateTotalPrice(Car car, RentalPeriod period, boolean withInsurance) {
        if (car == null) {
            throw new IllegalArgumentException("O carro não pode ser nulo.");
        }
        if (period == null) {
            throw new IllegalArgumentException("O período não pode ser nulo.");
        }

        long totalDays = period.getDays();
        BigDecimal dailyRate = BigDecimal.valueOf(car.basePrice());

        BigDecimal basePrice = dailyRate.multiply(BigDecimal.valueOf(totalDays));
        BigDecimal priceWithSurcharges = basePrice.add(calculateSurcharges(dailyRate, period));

        BigDecimal priceWithInsurance = applyInsurance(priceWithSurcharges, withInsurance);

        BigDecimal finalPrice = applyDiscounts(priceWithInsurance, totalDays);

        return finalPrice.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateSurcharges(BigDecimal dailyRate, RentalPeriod period) {
        BigDecimal totalSurcharge = BigDecimal.ZERO;
        for (LocalDate date = period.startDate(); date.isBefore(period.endDate()); date = date.plusDays(1)) {
            if (isWeekendOrHoliday(date)) {
                totalSurcharge = totalSurcharge.add(dailyRate.multiply(WEEKEND_HOLIDAY_SURCHARGE));
            }
        }
        return totalSurcharge;
    }

    private BigDecimal applyDiscounts(BigDecimal price, long totalDays) {
        if (totalDays > 15) {
            return price.subtract(price.multiply(DISCOUNT_ABOVE_15_DAYS));
        }
        if (totalDays >= 7) {
            return price.subtract(price.multiply(DISCOUNT_7_TO_14_DAYS));
        }
        return price;
    }

    private BigDecimal applyInsurance(BigDecimal price, boolean withInsurance) {
        if (withInsurance) {
            return price.add(price.multiply(INSURANCE_FEE));
        }
        return price;
    }
}