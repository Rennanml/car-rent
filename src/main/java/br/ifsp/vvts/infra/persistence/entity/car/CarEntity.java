package br.ifsp.vvts.infra.persistence.entity.car;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "car")
public class CarEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "license_plate", unique = true, nullable = false))
    private LicensePlateEmbeddable licensePlate;

    private String brand;
    private String model;
    private double basePrice;

    /**
     * @deprecated Construtor exigido pelo JPA. Não utilize.
     */
    @Deprecated
    public CarEntity() {
    }

    public CarEntity(Long id, LicensePlateEmbeddable licensePlate, String brand, String model, double basePrice) {
        this.id = id;
        this.licensePlate = licensePlate;
        this.brand = brand;
        this.model = model;
        this.basePrice = basePrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CarEntity carEntity = (CarEntity) o;
        return Objects.equals(id, carEntity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}