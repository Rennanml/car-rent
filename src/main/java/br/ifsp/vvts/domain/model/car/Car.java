package br.ifsp.vvts.domain.model.car;

public record Car(LicensePlate licensePlate, String brand, String model, double basePrice) {
    public Car {
        if (licensePlate == null) {
            throw new IllegalArgumentException("License plate cannot be null");
        }

        if (brand == null || brand.isBlank()) {
            throw new IllegalArgumentException("Brand cannot be blank");
        }

        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("Model cannot be blank");
        }

        if (basePrice <= 0) {
            throw new IllegalArgumentException("Base price must be positive");
        }
    }

    @Override
    public String toString() {
        return brand + " " + model + " - " + licensePlate.value();
    }
}