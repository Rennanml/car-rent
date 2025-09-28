package br.ifsp.vvts.domain.useCases;

import br.ifsp.vvts.domain.model.costumer.CPF;
import br.ifsp.vvts.domain.model.costumer.Costumer;
import br.ifsp.vvts.exception.EntityAlreadyExistsException;
import br.ifsp.vvts.infra.persistence.entity.costumer.CostumerEntity;
import br.ifsp.vvts.infra.persistence.mapper.CostumerMapper;
import br.ifsp.vvts.infra.persistence.repository.CostumerRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ManageInventoryUseCase {

    private final CostumerRepository costumerRepository;
    private final CostumerMapper costumerMapper;

    public ManageInventoryUseCase(CostumerRepository costumerRepository, CostumerMapper costumerMapper) {
        this.costumerRepository = costumerRepository;
        this.costumerMapper = costumerMapper;
    }

    @Transactional
    @Modifying
    public Costumer createCostumer(String name, String cpfNumber) {
        CPF cpf = CPF.of(cpfNumber);
        Costumer costumerToSave = new Costumer(name, cpf);

        costumerRepository.findByCpfNumber(cpf.unformat()).ifPresent(entity -> {
            throw new EntityAlreadyExistsException("A costumer with this CPF already exists.");
        });

        CostumerEntity entity = costumerMapper.toEntity(costumerToSave);
        CostumerEntity savedEntity = costumerRepository.save(entity);

        return costumerMapper.toDomain(savedEntity);
    }

    @Transactional
    @Modifying
    public Optional<Costumer> updateCostumer(Long id, String newName) {
        return costumerRepository.findById(id)
                .map(entity -> {
                    Costumer updatedDomainCostumer = new Costumer(newName, CPF.of(entity.getCpf().getNumber()));
                    entity.setName(updatedDomainCostumer.name());
                    CostumerEntity savedEntity = costumerRepository.save(entity);
                    return costumerMapper.toDomain(savedEntity);
                });
    }

    @Transactional
    @Modifying
    public boolean deleteCostumer(Long id) {
        if (costumerRepository.existsById(id)) {
            costumerRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public Optional<Costumer> findCostumerById(Long id) {
        return costumerRepository.findById(id)
                .map(costumerMapper::toDomain);
    }

    @Transactional
    public List<Costumer> getAllCostumers() {
        return costumerRepository.findAll().stream()
                .map(costumerMapper::toDomain)
                .collect(Collectors.toList());
    }
}