package br.com.raizesdonordeste.gestor.service;

import br.com.raizesdonordeste.gestor.domain.entity.Customer;
import br.com.raizesdonordeste.gestor.domain.entity.LoyaltyAccount;
import br.com.raizesdonordeste.gestor.exception.ConsentRequiredException;
import br.com.raizesdonordeste.gestor.repository.LoyaltyAccountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;


@Service
public class LoyaltyService {

    private static final BigDecimal POINTS_PER_CURRENCY_UNIT = BigDecimal.ONE;

    private final LoyaltyAccountRepository loyaltyAccountRepository;

    public LoyaltyService(LoyaltyAccountRepository loyaltyAccountRepository) {
        this.loyaltyAccountRepository = loyaltyAccountRepository;
    }

    public void accruePointsForOrder(Customer customer, BigDecimal orderTotal) {
        if (customer == null) {
            return; // pedido anonimo (ex.: totem sem cadastro) nao acumula pontos
        }
        if (!customer.isLgpdConsentGiven()) {
            throw new ConsentRequiredException(
                    "Cliente " + customer.getId() + " nao consentiu o uso de dados para fidelizacao");
        }
        LoyaltyAccount account = loyaltyAccountRepository.findByCustomerId(customer.getId())
                .orElseGet(() -> {
                    LoyaltyAccount created = new LoyaltyAccount();
                    created.setCustomer(customer);
                    return created;
                });
        int earned = orderTotal.multiply(POINTS_PER_CURRENCY_UNIT)
                .setScale(0, RoundingMode.DOWN)
                .intValue();
        account.setPoints(account.getPoints() + earned);
        loyaltyAccountRepository.save(account);
    }
}
