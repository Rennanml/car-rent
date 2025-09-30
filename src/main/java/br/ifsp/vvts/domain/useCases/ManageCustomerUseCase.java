package br.ifsp.vvts.domain.useCases;

import br.ifsp.vvts.domain.model.customer.CPF;
import br.ifsp.vvts.domain.model.customer.Customer;
import br.ifsp.vvts.exception.EntityAlreadyExistsException;
import br.ifsp.vvts.infra.persistence.entity.customer.CustomerEntity;
import br.ifsp.vvts.infra.persistence.mapper.CustomerMapper;
import br.ifsp.vvts.infra.persistence.repository.CustomerRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ManageCustomerUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;


    public ManageCustomerUseCase(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    @Transactional
    @Modifying
    public Customer createCustomer(String name, String cpfNumber) {
        CPF cpf = CPF.of(cpfNumber);
        Customer customerToSave = new Customer(name, cpf);

        customerRepository.findByCpfNumber(cpf.unformat()).ifPresent(entity -> {
            throw new EntityAlreadyExistsException("A customer with this CPF already exists.");
        });

        CustomerEntity entity = customerMapper.toEntity(customerToSave);
        CustomerEntity savedEntity = customerRepository.save(entity);

        return customerMapper.toDomain(savedEntity);
    }

    @Transactional
    @Modifying
    public Optional<Customer> updateCustomer(String customerCpf, String newName) {
        var cpf = CPF.of(customerCpf);
        return customerRepository.findByCpfNumber(cpf.unformat())
                .map(entity -> {
                    Customer updatedDomainCustomer = new Customer(newName, CPF.of(entity.getCpf().getNumber()));
                    entity.setName(updatedDomainCustomer.name());
                    CustomerEntity savedEntity = customerRepository.save(entity);
                    return customerMapper.toDomain(savedEntity);
                });
    }

    @Transactional
    @Modifying
    public boolean deleteCustomer(String cpfNumber) {
        CPF cpf = CPF.of(cpfNumber);
        return customerRepository.findByCpfNumber(cpf.unformat())
                .map(entity -> {
                    customerRepository.delete(entity);
                    return true;
                }).orElse(false);
    }

    @Transactional
    public Optional<Customer> findCustomerByCpf(String cpfNumber) {
        CPF cpf = CPF.of(cpfNumber);
        return customerRepository.findByCpfNumber(cpf.unformat())
                .map(customerMapper::toDomain);
    }

    @Transactional
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(customerMapper::toDomain)
                .collect(Collectors.toList());
    }


}