package br.ifsp.vvts.domain.model.customer;

import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;


public class CPF implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final Pattern DIGITS = Pattern.compile("\\d+");
    private final String digits;

    private CPF(String digits) {
        this.digits = digits;
    }

    public static CPF of(String raw) {
        String d = normalize(raw);
        if (!isValidDigits(d)) {
            throw new IllegalArgumentException("Invalid CPF: " + raw);
        }
        return new CPF(d);
    }

    public static Optional<CPF> tryOf(String raw) {
        String d = normalize(raw);
        if (!isValidDigits(d)) return Optional.empty();
        return Optional.of(new CPF(d));
    }

    public String unformat() {
        return digits;
    }

    @JsonValue
    public String format() {
        return String.format("%s.%s.%s-%s",
                digits.substring(0, 3),
                digits.substring(3, 6),
                digits.substring(6, 9),
                digits.substring(9, 11));
    }

    @Override
    public String toString() {
        return format();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CPF)) return false;
        CPF cpf = (CPF) o;
        return digits.equals(cpf.digits);
    }

    @Override
    public int hashCode() {
        return Objects.hash(digits);
    }

    private static String normalize(String raw) {
        if (raw == null) return "";
        String onlyDigits = raw.replaceAll("[^0-9]", "");
        return onlyDigits;
    }

    public static boolean isValid(String raw) {
        String d = normalize(raw);
        return isValidDigits(d);
    }

    private static boolean isValidDigits(String d) {
        if (d.length() != 11) return false;
        boolean allSame = true;
        for (int i = 1; i < d.length(); i++) {
            if (d.charAt(i) != d.charAt(0)) { allSame = false; break; }
        }
        if (allSame) return false;

        int[] nums = new int[11];
        for (int i = 0; i < 11; i++) nums[i] = d.charAt(i) - '0';

        int sum = 0;
        for (int i = 0; i < 9; i++) sum += nums[i] * (10 - i);
        int r = sum % 11;
        int dig1 = (r < 2) ? 0 : 11 - r;
        if (dig1 != nums[9]) return false;

        sum = 0;
        for (int i = 0; i < 10; i++) sum += nums[i] * (11 - i);
        r = sum % 11;
        int dig2 = (r < 2) ? 0 : 11 - r;
        return dig2 == nums[10];
    }
}
