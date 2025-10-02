package br.ifsp.vvts.infra.persistence.entity.car;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class LicensePlateEmbeddable implements Serializable {

    private String value;

    /**
     * @deprecated Construtor exigido pelo JPA. Não utilize.
     */
    @Deprecated
    public LicensePlateEmbeddable() {
    }

    public LicensePlateEmbeddable(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LicensePlateEmbeddable that = (LicensePlateEmbeddable) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}