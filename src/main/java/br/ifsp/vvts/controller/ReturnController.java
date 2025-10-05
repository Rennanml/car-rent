package br.ifsp.vvts.controller;

import br.ifsp.vvts.domain.dto.ReturnCarRequest;
import br.ifsp.vvts.domain.model.rental.Rental;
import br.ifsp.vvts.domain.useCases.ReturnCarUseCase;
import br.ifsp.vvts.security.auth.AuthenticationInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/returns")
public class ReturnController {

    private final ReturnCarUseCase returnCarUseCase;
    private final AuthenticationInfoService authService;

    public ReturnController(ReturnCarUseCase returnCarUseCase, AuthenticationInfoService authService) {
        this.returnCarUseCase = returnCarUseCase;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<Rental> returnCar(@RequestBody ReturnCarRequest request) {
        authService.getAuthenticatedUserId();

        Rental returnedRental = returnCarUseCase.execute(request);
        return ResponseEntity.ok(returnedRental);
    }
}