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

        domainObject.setActualReturnDate(entity.getActualReturnDate());
        domainObject.setFinalPrice(entity.getFinalPrice());

        return domainObject;
    }

    public RentalEntity toEntity(Rental rental) {
        if (rental == null) {
            return null;
        }
        RentalEntity entity = new RentalEntity();

        entity.setId(rental.getId());
        entity.setCustomer(customerMapper.toEntity(rental.getCustomer()));
        entity.setCar(carMapper.toEntity(rental.getCar()));

        if (rental.getPeriod() != null) {
            entity.setStartDate(rental.getPeriod().startDate());
            entity.setEndDate(rental.getPeriod().endDate());
        }

        entity.setTotalPrice(rental.getTotalPrice());
        entity.setStatus(rental.getStatus());

        entity.setActualReturnDate(rental.getActualReturnDate());
        entity.setFinalPrice(rental.getFinalPrice());

        return entity;
    }
}