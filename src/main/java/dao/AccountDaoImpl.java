package dao;

import model.Account;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class AccountDaoImpl implements AccountDao {

    private AtomicLong accNumber = new AtomicLong(0);
    private ConcurrentHashMap<Long, Account> accountConcurrentHashMap;

    public AccountDaoImpl() {
        accountConcurrentHashMap = new ConcurrentHashMap<Long, Account>();
    }

    @Override
    public long insertAccount(BigDecimal balance, BigDecimal overdraftLimit, Currency currency) {
        Long accountNumber = accNumber.incrementAndGet();
        Account account = new Account(accountNumber, balance, overdraftLimit, currency);
        accountConcurrentHashMap.put(accountNumber, account);
        return accountNumber;
    }

    @Override
    public @Nullable Account getAccount(long accountNumber) {
        return accountConcurrentHashMap.get(accountNumber);
    }

    @Override
    public @Nullable List<Account> getAccounts() {
        return new ArrayList<>(accountConcurrentHashMap.values());
    }

    @Override
    public boolean updateAccount(Account account) {
        if (accountConcurrentHashMap.containsKey(account.getAccountNumber())) {
            accountConcurrentHashMap.put(account.getAccountNumber(), account);
            return true;
        } else {
            return false;
        }

    }

    @Override
    public boolean deleteAccount(long accountNumber) {
        if (accountConcurrentHashMap.containsKey(accountNumber)) {
            accountConcurrentHashMap.remove(accountNumber);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean accountExists(long accountNumber) {
        return accountConcurrentHashMap.containsKey(accountNumber);
    }
}
