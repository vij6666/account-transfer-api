package service;

import exceptions.TransferException;
import model.Account;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import org.jetbrains.annotations.Nullable;

public interface AccountService {

    Account addAccount(BigDecimal balance, BigDecimal overdraftLimit, Currency currency);

    @Nullable
    Account getAccount(long accountNumber) throws TransferException;

    void transferMoney(long fromAccountNumber, long toAccountNumber, BigDecimal amount) throws TransferException;

    List<Account> getAccounts();

    boolean editAccount(Account account) throws TransferException;

    boolean deleteAccount(long accountNumber) throws TransferException;

    boolean accountExists(long accountNumber);
}
