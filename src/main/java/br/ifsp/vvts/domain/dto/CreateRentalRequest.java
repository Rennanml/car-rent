package br.ifsp.vvts.domain.dto;

import java.time.LocalDate;

public record CreateRentalRequest(String licensePlate, String cpf, LocalDate startDate, LocalDate endDate, boolean withInsurance) {
}