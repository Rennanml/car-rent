package br.ifsp.vvts.domain.model.customer;

public record Customer(String name, CPF cpf) {
    public Customer {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Customer name cannot be blank");
        }
        if (cpf == null) {
            throw new IllegalArgumentException("Customer CPF cannot be null");
        }
    }

    @Override
    public String toString() {
        return name + " - " + cpf.format();
    }
}
