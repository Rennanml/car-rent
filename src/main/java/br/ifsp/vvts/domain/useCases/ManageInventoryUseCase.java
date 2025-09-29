package br.ifsp.vvts.domain.useCases;

import br.ifsp.vvts.domain.model.car.Car;
import br.ifsp.vvts.domain.model.car.LicensePlate;
import br.ifsp.vvts.domain.model.customer.CPF;
import br.ifsp.vvts.domain.model.customer.Customer;
import br.ifsp.vvts.exception.EntityAlreadyExistsException;
import br.ifsp.vvts.infra.persistence.entity.car.CarEntity;
import br.ifsp.vvts.infra.persistence.entity.customer.CustomerEntity;
import br.ifsp.vvts.infra.persistence.mapper.CarMapper;
import br.ifsp.vvts.infra.persistence.mapper.CustomerMapper;
import br.ifsp.vvts.infra.persistence.repository.CarRepository;
import br.ifsp.vvts.infra.persistence.repository.CustomerRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ManageInventoryUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final CarRepository carRepository;
    private final CarMapper carMapper;


    public ManageInventoryUseCase(CustomerRepository customerRepository, CustomerMapper customerMapper, CarRepository carRepository, CarMapper carMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
        this.carRepository = carRepository;
        this.carMapper = carMapper;
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


    @Transactional
    @Modifying
    public Car createCar(String licensePlateValue, String brand, String model, double basePrice) {
        LicensePlate licensePlate = LicensePlate.of(licensePlateValue);
        Car carToSave = new Car(licensePlate, brand, model, basePrice);

        carRepository.findByLicensePlate(licensePlate.value()).ifPresent(entity -> {
            throw new EntityAlreadyExistsException("A car with this license plate already exists.");
        });

        CarEntity entity = carMapper.toEntity(carToSave);
        CarEntity savedEntity = carRepository.save(entity);

        return carMapper.toDomain(savedEntity);
    }

    @Transactional
    @Modifying
    public Optional<Car> updateCar(String licensePlateValue, String brand, String model, double basePrice) {
        if (brand == null || brand.isBlank()) {
            throw new IllegalArgumentException("Car brand cannot be blank");
        }

        LicensePlate licensePlate = LicensePlate.of(licensePlateValue);
        return carRepository.findByLicensePlate(licensePlate.value())
                .map(entity -> {
                    entity.setBrand(brand);
                    entity.setModel(model);
                    entity.setBasePrice(basePrice);
                    CarEntity savedEntity = carRepository.save(entity);
                    return carMapper.toDomain(savedEntity);
                });
    }

    @Transactional
    @Modifying
    public boolean deleteCar(String licensePlateValue) {
        LicensePlate licensePlate = LicensePlate.of(licensePlateValue);
        return carRepository.findByLicensePlate(licensePlate.value())
                .map(carEntity -> {
                    carRepository.delete(carEntity);
                    return true;
                }).orElse(false);
    }

    @Transactional
    public Optional<Car> findCarByLicensePlate(String licensePlateValue) {
        LicensePlate licensePlate = LicensePlate.of(licensePlateValue);
        return carRepository.findByLicensePlate(licensePlate.value())
                .map(carMapper::toDomain);
    }

    @Transactional
    public List<Car> getAllCars() {
        return carRepository.findAll().stream()
                .map(carMapper::toDomain)
                .collect(Collectors.toList());
    }
}