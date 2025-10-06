package br.ifsp.vvts.infra.persistence.entity.customer;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "customer")
public class CustomerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Embedded
    @AttributeOverride(name = "number", column = @Column(name = "cpf_number", unique = true, nullable = false))
    private CPFEmbeddable cpf;

    /**
     * @deprecated Construtor exigido pelo JPA. Não utilize.
     */
    @Deprecated
    public CustomerEntity() {
    }

    public CustomerEntity(Long id, String name, CPFEmbeddable cpf) {
        this.id = id;
        this.name = name;
        this.cpf = cpf;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomerEntity that = (CustomerEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}