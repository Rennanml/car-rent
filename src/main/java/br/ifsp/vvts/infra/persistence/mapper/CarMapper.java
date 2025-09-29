package br.ifsp.vvts.infra.persistence.mapper;

import br.ifsp.vvts.domain.model.car.Car;
import br.ifsp.vvts.domain.model.car.LicensePlate;
import br.ifsp.vvts.infra.persistence.entity.car.CarEntity;
import br.ifsp.vvts.infra.persistence.entity.car.LicensePlateEmbeddable;
import org.springframework.stereotype.Component;

@Component
public class CarMapper {
    public CarEntity toEntity(Car domainObject) {
        if (domainObject == null) {
            return null;
        }

        LicensePlateEmbeddable licensePlateEmbeddable = new LicensePlateEmbeddable(domainObject.licensePlate().value());

        return new CarEntity(null, licensePlateEmbeddable, domainObject.brand(), domainObject.model(), domainObject.basePrice());
    }

    public Car toDomain(CarEntity entity) {
        if (entity == null) {
            return null;
        }

        LicensePlate licensePlate = LicensePlate.of(entity.getLicensePlate().getValue());
        return new Car(licensePlate, entity.getBrand(), entity.getModel(), entity.getBasePrice());
    }
}