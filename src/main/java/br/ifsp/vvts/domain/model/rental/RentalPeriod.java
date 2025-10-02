package br.ifsp.vvts.domain.model.rental;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public record RentalPeriod(LocalDate startDate, LocalDate endDate) {
    public RentalPeriod {
        Objects.requireNonNull(startDate, "Data de início obrigatória");
        Objects.requireNonNull(endDate, "Data de fim obrigatória");
        if (startDate.isAfter(endDate) || startDate.isEqual(endDate)) {
            throw new IllegalArgumentException("Período de aluguel inválido: a data final deve ser após a data inicial.");
        }
        if (ChronoUnit.DAYS.between(startDate, endDate) > 60) {
            throw new IllegalArgumentException("Período de aluguel inválido: o intervalo não pode ser maior que 60 dias.");
        }
    }

    public long getDays() {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }
}