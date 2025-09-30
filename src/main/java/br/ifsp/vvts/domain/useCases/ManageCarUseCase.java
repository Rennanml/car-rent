package br.ifsp.vvts.domain.useCases;

import br.ifsp.vvts.domain.model.car.Car;
import br.ifsp.vvts.domain.model.car.LicensePlate;
import br.ifsp.vvts.exception.EntityAlreadyExistsException;
import br.ifsp.vvts.infra.persistence.entity.car.CarEntity;
import br.ifsp.vvts.infra.persistence.mapper.CarMapper;
import br.ifsp.vvts.infra.persistence.repository.CarRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ManageCarUseCase {
    private final CarRepository carRepository;
    private final CarMapper carMapper;


    public ManageCarUseCase(CarRepository carRepository, CarMapper carMapper) {
        this.carRepository = carRepository;
        this.carMapper = carMapper;
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
