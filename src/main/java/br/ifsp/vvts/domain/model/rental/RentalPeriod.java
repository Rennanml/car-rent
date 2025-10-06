package br.ifsp.vvts.domain.model.rental;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public record RentalPeriod(LocalDate startDate, LocalDate endDate) {
    public RentalPeriod {
        Objects.requireNonNull(startDate, "Mandatory start date.");
        Objects.requireNonNull(endDate, "Mandatory end date.");
        if (startDate.isAfter(endDate) || startDate.isEqual(endDate)) {
            throw new IllegalArgumentException("Invalid rental period: End date must be after start date.");
        }
        if (ChronoUnit.DAYS.between(startDate, endDate) > 60) {
            throw new IllegalArgumentException("Invalid rental period: The interval cannot be longer than 60 days.");
        }
    }

    public long getDays() {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }
}