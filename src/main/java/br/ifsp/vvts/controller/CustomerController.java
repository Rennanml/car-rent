package br.ifsp.vvts.controller;

import br.ifsp.vvts.domain.dto.CreateCustomerRequest;
import br.ifsp.vvts.domain.dto.UpdateCustomerRequest;
import br.ifsp.vvts.domain.model.customer.Customer;
import br.ifsp.vvts.domain.useCases.ManageCustomerUseCase;
import br.ifsp.vvts.security.auth.AuthenticationInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final ManageCustomerUseCase manageCustomerUseCase;
    private final AuthenticationInfoService authService;

    public CustomerController(ManageCustomerUseCase manageCustomerUseCase, AuthenticationInfoService authService) {
        this.manageCustomerUseCase = manageCustomerUseCase;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<Customer> create(@RequestBody CreateCustomerRequest request, UriComponentsBuilder uriBuilder) {
        authService.getAuthenticatedUserId();

        Customer createdCustomer = manageCustomerUseCase.createCustomer(request.name(), request.cpf());

        URI location = uriBuilder.path("/api/v1/customers/{cpf}").buildAndExpand(createdCustomer.cpf().format()).toUri();
        return ResponseEntity.created(location).body(createdCustomer);
    }

    @GetMapping
    public ResponseEntity<List<Customer>> listAll() {
        authService.getAuthenticatedUserId();

        List<Customer> allCustomers = manageCustomerUseCase.getAllCustomers();
        return ResponseEntity.ok(allCustomers);
    }

    @GetMapping("/{cpf}")
    public ResponseEntity<Customer> findByCpf(@PathVariable String cpf) {
        authService.getAuthenticatedUserId();

        return manageCustomerUseCase.findCustomerByCpf(cpf)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{cpf}")
    public ResponseEntity<Customer> update(@PathVariable String cpf, @RequestBody UpdateCustomerRequest request) {
        authService.getAuthenticatedUserId();

        return manageCustomerUseCase.updateCustomer(cpf, request.name())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{cpf}")
    public ResponseEntity<Void> delete(@PathVariable String cpf) {
        authService.getAuthenticatedUserId();

        boolean wasDeleted = manageCustomerUseCase.deleteCustomer(cpf);

        if (wasDeleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}