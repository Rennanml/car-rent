package br.ifsp.vvts.domain.model.costumer;

public record Costumer(String name, CPF cpf) {
    public Costumer {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Costumer name cannot be blank");
        }
        if (cpf == null) {
            throw new IllegalArgumentException("Costumer CPF cannot be null");
        }
    }

    @Override
    public String toString() {
        return name + " - " + cpf.format();
    }
}
