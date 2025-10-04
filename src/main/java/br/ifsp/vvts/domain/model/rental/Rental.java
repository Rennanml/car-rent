package br.ifsp.vvts.domain.model.rental;

import br.ifsp.vvts.domain.model.car.Car;
import br.ifsp.vvts.domain.model.customer.Customer;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter
public class Rental {
    private Long id;
    private Customer customer;
    private Car car;
    private RentalPeriod period;
    private BigDecimal totalPrice;
    private RentalStatus status;
    private LocalDate actualReturnDate;
    private BigDecimal finalPrice;
}
