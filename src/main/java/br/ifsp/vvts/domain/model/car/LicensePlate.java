package br.ifsp.vvts.domain.model.car;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;
import java.util.regex.Pattern;

public final class LicensePlate {
    private static final Pattern PATTERN = Pattern.compile("[A-Z]{3}[0-9][0-9A-Z][0-9]{2}");

    private final String value;

    private LicensePlate(String value) {
        this.value = value;
    }

    public static LicensePlate of(String value) {
        if (value == null || !PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid license plate: " + value);
        }
        return new LicensePlate(value);
    }

    @JsonValue
    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LicensePlate that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}

