package br.ifsp.vvts.controller;

import br.ifsp.vvts.domain.dto.CreateRentalRequest;
import br.ifsp.vvts.domain.dto.UpdateRentalStatusRequest;
import br.ifsp.vvts.domain.model.rental.Rental;
import br.ifsp.vvts.domain.useCases.CarRentUseCase;
import br.ifsp.vvts.domain.useCases.ManageRentalUseCase;
import br.ifsp.vvts.security.auth.AuthenticationInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/rentals")
public class RentalController {

    private final CarRentUseCase carRentUseCase;
    private final ManageRentalUseCase manageRentalUseCase;
    private final AuthenticationInfoService authService;

    public RentalController(CarRentUseCase carRentUseCase, ManageRentalUseCase manageRentalUseCase, AuthenticationInfoService authService) {
        this.carRentUseCase = carRentUseCase;
        this.manageRentalUseCase = manageRentalUseCase;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<Rental> create(@RequestBody CreateRentalRequest request, UriComponentsBuilder uriBuilder) {
        authService.getAuthenticatedUserId();

        Rental newRental = carRentUseCase.execute(
                request.licensePlate(),
                request.cpf(),
                request.startDate(),
                request.endDate(),
                request.withInsurance()
        );

        URI location = uriBuilder.path("/api/v1/rentals/{id}").buildAndExpand(newRental.getId()).toUri();
        return ResponseEntity.created(location).body(newRental);
    }

    @GetMapping
    public ResponseEntity<List<Rental>> listAll() {
        authService.getAuthenticatedUserId();
        List<Rental> allRentals = manageRentalUseCase.getAllRentals();
        return ResponseEntity.ok(allRentals);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rental> findById(@PathVariable Long id) {
        authService.getAuthenticatedUserId();
        return manageRentalUseCase.findRentalById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Rental> updateStatus(@PathVariable Long id, @RequestBody UpdateRentalStatusRequest request) {
        authService.getAuthenticatedUserId();
        Optional<Rental> updatedRental = manageRentalUseCase.updateRentalStatus(id, request.status());

        return updatedRental
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        authService.getAuthenticatedUserId();
        boolean wasDeleted = manageRentalUseCase.deleteRental(id);

        if (wasDeleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}