
package br.ifsp.vvts.infra.persistence.entity.costumer;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CPFEmbeddable implements Serializable {

    private String number;

    /**
     * @deprecated Construtor exigido pelo JPA. Não utilize.
     */
    @Deprecated
    public CPFEmbeddable() {
    }

    public CPFEmbeddable(String number) {
        this.number = number;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CPFEmbeddable that = (CPFEmbeddable) o;
        return Objects.equals(number, that.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }
}
