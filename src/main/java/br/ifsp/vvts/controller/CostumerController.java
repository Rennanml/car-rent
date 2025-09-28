package br.ifsp.vvts.controller;

import br.ifsp.vvts.domain.dto.CreateCostumerRequest;
import br.ifsp.vvts.domain.dto.UpdateCostumerRequest;
import br.ifsp.vvts.domain.model.costumer.Costumer;
import br.ifsp.vvts.domain.useCases.ManageInventoryUseCase;
import br.ifsp.vvts.security.auth.AuthenticationInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
public class CostumerController {

    private final ManageInventoryUseCase manageInventoryUseCase;
    private final AuthenticationInfoService authService;

    public CostumerController(ManageInventoryUseCase manageInventoryUseCase, AuthenticationInfoService authService) {
        this.manageInventoryUseCase = manageInventoryUseCase;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<Costumer> create(@RequestBody CreateCostumerRequest request, UriComponentsBuilder uriBuilder) {
        authService.getAuthenticatedUserId();

        Costumer createdCostumer = manageInventoryUseCase.createCostumer(request.name(), request.cpf());

        URI location = uriBuilder.path("/api/v1/customers/{cpf}").buildAndExpand(createdCostumer.cpf().format()).toUri();
        return ResponseEntity.created(location).body(createdCostumer);
    }

    @GetMapping
    public ResponseEntity<List<Costumer>> listAll() {
        authService.getAuthenticatedUserId();

        List<Costumer> allCostumers = manageInventoryUseCase.getAllCostumers();
        return ResponseEntity.ok(allCostumers);
    }

    @GetMapping("/{cpf}")
    public ResponseEntity<Costumer> findByCpf(@PathVariable String cpf) {
        authService.getAuthenticatedUserId();

        return manageInventoryUseCase.findCostumerByCpf(cpf)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{cpf}")
    public ResponseEntity<Costumer> update(@PathVariable String cpf, @RequestBody UpdateCostumerRequest request) {
        authService.getAuthenticatedUserId();

        return manageInventoryUseCase.updateCostumer(cpf, request.name())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{cpf}")
    public ResponseEntity<Void> delete(@PathVariable String cpf) {
        authService.getAuthenticatedUserId();

        boolean wasDeleted = manageInventoryUseCase.deleteCostumer(cpf);

        if (wasDeleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}