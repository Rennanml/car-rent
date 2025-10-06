package br.ifsp.vvts.domain.dto;

import java.time.LocalDate;

public record ReturnCarRequest(
        Long rentalId,
        LocalDate actualReturnDate,
        boolean needsMaintenance,
        boolean needsCleaning
) {}