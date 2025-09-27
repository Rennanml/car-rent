package br.ifsp.vvts.infra.persistence.mapper;

import br.ifsp.vvts.domain.model.costumer.CPF;
import br.ifsp.vvts.domain.model.costumer.Costumer;
import br.ifsp.vvts.infra.persistence.entity.costumer.CPFEmbeddable;
import br.ifsp.vvts.infra.persistence.entity.costumer.CostumerEntity;
import org.springframework.stereotype.Component;

@Component
public class CostumerMapper {
    public CostumerEntity toEntity(Costumer domainObject) {
        if (domainObject == null) {
            return null;
        }

        CPFEmbeddable cpfEmbeddable = new CPFEmbeddable(domainObject.cpf().unformat());

        return new CostumerEntity(null, domainObject.name(), cpfEmbeddable);
    }

    public Costumer toDomain(CostumerEntity entity) {
        if (entity == null) {
            return null;
        }

        CPF cpf = CPF.of(entity.getCpf().getNumber());
        return new Costumer(entity.getName(), cpf);
    }
}
