package br.ifsp.vvts.infra.persistence.mapper;

import br.ifsp.vvts.domain.model.rental.Rental;
import br.ifsp.vvts.domain.model.rental.RentalPeriod;
import br.ifsp.vvts.infra.persistence.entity.rental.RentalEntity;
import org.springframework.stereotype.Component;

@Component
public class RentalMapper {

    private final CustomerMapper customerMapper;
    private final CarMapper carMapper;

    public RentalMapper(CustomerMapper customerMapper, CarMapper carMapper) {
        this.customerMapper = customerMapper;
        this.carMapper = carMapper;
    }

    public Rental toDomain(RentalEntity entity) {
        if (entity == null) {
            return null;
        }

        Rental domainObject = new Rental();
        domainObject.setId(entity.getId());
        domainObject.setCustomer(customerMapper.toDomain(entity.getCustomer()));
        domainObject.setCar(carMapper.toDomain(entity.getCar()));
        domainObject.setPeriod(new RentalPeriod(entity.getStartDate(), entity.getEndDate()));
        domainObject.setTotalPrice(entity.getTotalPrice());
        domainObject.setStatus(entity.getStatus());

        return domainObject;
    }
}