package br.com.raizesdonordeste.gestor.controller;

import br.com.raizesdonordeste.gestor.domain.entity.Customer;
import br.com.raizesdonordeste.gestor.dto.ConsentRequest;
import br.com.raizesdonordeste.gestor.exception.ResourceNotFoundException;
import br.com.raizesdonordeste.gestor.repository.CustomerRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/customers/{customerId}/consent")
public class CustomerController {

    private final CustomerRepository customerRepository;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @PutMapping
    public void setConsent(@PathVariable Long customerId, @Valid @RequestBody ConsentRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente nao encontrado: " + customerId));
        customer.setLgpdConsentGiven(request.consentGiven());
        customer.setLgpdConsentTimestamp(request.consentGiven() ? Instant.now() : null);
        customerRepository.save(customer);
    }
}
