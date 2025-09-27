package br.ifsp.vvts.domain.model.car;

public record Car(
        LicensePlate licensePlate,
        String model,
        String color
) {
    public Car {
        if (licensePlate == null) {
            throw new IllegalArgumentException("Licence plate cannot be null");
        }

        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("Model cannot be blank");
        }

        if (color == null || color.isBlank()) {
            throw new IllegalArgumentException("Color cannot be null");
        }
    }

    @Override
    public String toString() {
        return model + " " + color + " - " + licensePlate.toString();
    }
}
