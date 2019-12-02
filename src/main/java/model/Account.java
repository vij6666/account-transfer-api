package model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Currency;

@Data
@NoArgsConstructor
public class Account {

    private Long accountNumber;
    private BigDecimal balance;
    private BigDecimal overdraftLimit;
    private Currency currency;

    public Account(long accountNumber, BigDecimal balance, BigDecimal overdraftLimit, Currency currency) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.overdraftLimit = overdraftLimit;
        this.currency = currency;
    }

}
