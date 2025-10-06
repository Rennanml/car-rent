package br.ifsp.vvts.controller;

import br.ifsp.vvts.domain.dto.CreateCarRequest;
import br.ifsp.vvts.domain.dto.UpdateCarRequest;
import br.ifsp.vvts.domain.model.car.Car;
import br.ifsp.vvts.domain.useCases.ManageCarUseCase;
import br.ifsp.vvts.security.auth.AuthenticationInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cars")
public class CarController {

    private final ManageCarUseCase manageCarUseCase;
    private final AuthenticationInfoService authService;

    public CarController(ManageCarUseCase manageCarUseCase, AuthenticationInfoService authService) {
        this.manageCarUseCase = manageCarUseCase;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<Car> create(@RequestBody CreateCarRequest request, UriComponentsBuilder uriBuilder) {
        authService.getAuthenticatedUserId();

        Car createdCar = manageCarUseCase.createCar(request.licensePlate(), request.brand(), request.model(), request.basePrice());

        URI location = uriBuilder.path("/api/v1/cars/{licensePlate}").buildAndExpand(createdCar.licensePlate().value()).toUri();
        return ResponseEntity.created(location).body(createdCar);
    }

    @GetMapping
    public ResponseEntity<List<Car>> listAll() {
        authService.getAuthenticatedUserId();

        List<Car> allCars = manageCarUseCase.getAllCars();
        return ResponseEntity.ok(allCars);
    }

    @GetMapping("/{licensePlate}")
    public ResponseEntity<Car> findByLicensePlate(@PathVariable String licensePlate) {
        authService.getAuthenticatedUserId();

        return manageCarUseCase.findCarByLicensePlate(licensePlate)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{licensePlate}")
    public ResponseEntity<Car> update(@PathVariable String licensePlate, @RequestBody UpdateCarRequest request) {
        authService.getAuthenticatedUserId();

        return manageCarUseCase.updateCar(licensePlate, request.brand(), request.model(), request.basePrice())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{licensePlate}")
    public ResponseEntity<Void> delete(@PathVariable String licensePlate) {
        authService.getAuthenticatedUserId();

        boolean wasDeleted = manageCarUseCase.deleteCar(licensePlate);

        if (wasDeleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}