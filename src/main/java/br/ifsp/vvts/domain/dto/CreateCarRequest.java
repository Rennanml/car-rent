package br.ifsp.vvts.domain.dto;

public record CreateCarRequest(String licensePlate, String brand, String model, double basePrice) {
}