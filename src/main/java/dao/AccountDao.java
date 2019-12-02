package dao;

import model.Account;

import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

public interface AccountDao {

    long insertAccount(BigDecimal balance, BigDecimal overdraftLimit, Currency currency);

    @Nullable
    Account getAccount(long accountNumber);

    @Nullable
    List<Account> getAccounts();

    boolean updateAccount(Account account);

    boolean deleteAccount(long accountNumber);

    boolean accountExists(long accountNumber);
}
