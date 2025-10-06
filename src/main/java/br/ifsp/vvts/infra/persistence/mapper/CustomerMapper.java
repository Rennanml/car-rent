package br.ifsp.vvts.infra.persistence.mapper;

import br.ifsp.vvts.domain.model.customer.CPF;
import br.ifsp.vvts.domain.model.customer.Customer;
import br.ifsp.vvts.infra.persistence.entity.customer.CPFEmbeddable;
import br.ifsp.vvts.infra.persistence.entity.customer.CustomerEntity;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {
    public CustomerEntity toEntity(Customer domainObject) {
        if (domainObject == null) {
            return null;
        }

        CPFEmbeddable cpfEmbeddable = new CPFEmbeddable(domainObject.cpf().unformat());

        return new CustomerEntity(null, domainObject.name(), cpfEmbeddable);
    }

    public Customer toDomain(CustomerEntity entity) {
        if (entity == null) {
            return null;
        }

        CPF cpf = CPF.of(entity.getCpf().getNumber());
        return new Customer(entity.getName(), cpf);
    }
}
