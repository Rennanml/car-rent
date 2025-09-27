package br.ifsp.vvts.infra.persistence.entity.costumer;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "costumer")
public class CostumerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Embedded
    @AttributeOverride(name = "number", column = @Column(name = "cpf_number"))
    private CPFEmbeddable cpf;

    /**
     * @deprecated Construtor exigido pelo JPA. Não utilize.
     */
    @Deprecated
    public CostumerEntity() {
    }

    public CostumerEntity(Long id, String name, CPFEmbeddable cpf) {
        this.id = id;
        this.name = name;
        this.cpf = cpf;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CostumerEntity that = (CostumerEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}