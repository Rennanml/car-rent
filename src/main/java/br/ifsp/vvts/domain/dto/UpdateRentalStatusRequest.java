package br.ifsp.vvts.domain.dto;

import br.ifsp.vvts.domain.model.rental.RentalStatus;

public record UpdateRentalStatusRequest(RentalStatus status) {
}