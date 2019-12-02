package service;

import dao.AccountDao;
import exceptions.ErrorCode;
import exceptions.TransferException;
import model.Account;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AccountServiceImpl implements AccountService {

    private final AccountDao accountDao;
    private Map<Long, Boolean> accountNumberBlockedFlag = new ConcurrentHashMap<>();

    public AccountServiceImpl(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    @Override
    public Account addAccount(BigDecimal balance, BigDecimal overdraftLimit, Currency currency) {
        long accountNumber = accountDao.insertAccount(balance, overdraftLimit, currency);
        return accountDao.getAccount(accountNumber);
    }

    @Override
    public @Nullable Account getAccount(long accountNumber) throws TransferException {

        Account account = accountDao.getAccount(accountNumber);
        if(account != null) {
            return account;
        } else {
            throw new TransferException("Account does not exist", ErrorCode.INVALID_ACCOUNT);
        }

    }

    @Override
    public @Nullable List<Account> getAccounts() {
        return accountDao.getAccounts();
    }

    @Override
    public boolean editAccount(Account account) throws TransferException {
        synchronized (this) {

            if (accountNumberBlockedFlag.containsKey(account.getAccountNumber())){
                throw new TransferException("Operation can't be executed, because "
                        + "previous transaction related with this account is not finished", ErrorCode.CONCURRENT_ACCESS);
            } else {
                accountNumberBlockedFlag.put(account.getAccountNumber(), true);
            }
            try{
                return accountDao.updateAccount(account);
            }finally {
                synchronized (this) {
                    accountNumberBlockedFlag.remove(account.getAccountNumber());
                }
            }
        }

    }

    @Override
    public boolean deleteAccount(long accountNumber) throws TransferException{
        try {
            Account account = getAccount(accountNumber);
        } catch (TransferException e){
            throw e;
        }
        return accountDao.deleteAccount(accountNumber);
    }

    @Override
    public boolean accountExists(long accountNumber) {
        return accountDao.accountExists(accountNumber);
    }

    @Override
    public void transferMoney(long fromAccountNumber, long toAccountNumber, BigDecimal amount) throws TransferException {
        if (fromAccountNumber == toAccountNumber) {
            throw new TransferException("Account can't transfer money to itself",
                    ErrorCode.TRANSFER_ON_SAME_ACCOUNT);
        }
        if (amount.compareTo(new BigDecimal(0)) < 0) {
            throw new TransferException("Passed amount is negative", ErrorCode.NEGATIVE_AMOUNT);
        }

        synchronized (this) {
            if (accountNumberBlockedFlag.containsKey(fromAccountNumber) ||
                    accountNumberBlockedFlag.containsKey(toAccountNumber)) {
                throw new TransferException("Operation can't be executed, because "
                        + "previous transaction related with this account is not finished", ErrorCode.CONCURRENT_ACCESS);
            } else {
                accountNumberBlockedFlag.put(fromAccountNumber, true);
                accountNumberBlockedFlag.put(toAccountNumber, true);
            }
        }
        try {
            Account fromAccount = accountDao.getAccount(fromAccountNumber);
            if (fromAccount == null) {
                throw new TransferException("Account with id" + fromAccount + " can't be found",
                        ErrorCode.INVALID_ACCOUNT);
            }
            if ((fromAccount.getBalance().add(fromAccount.getOverdraftLimit())).compareTo(amount) < 0) {
                throw new TransferException(
                        String.format("Account=%s does not have the required funds=%s", fromAccount.getAccountNumber(), fromAccount.getBalance().add(fromAccount.getOverdraftLimit())),
                        ErrorCode.INSUFFICIENT_FUNDS);
            }
            Account toAccount = accountDao.getAccount(toAccountNumber);
            if (toAccount == null) {
                throw new TransferException("Account with id" + toAccount + " can't be found",
                        ErrorCode.INVALID_ACCOUNT);
            }

            fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
            toAccount.setBalance(toAccount.getBalance().add(amount));

            accountDao.updateAccount(fromAccount);
            accountDao.updateAccount(toAccount);
        } finally {
            synchronized (this) {
                accountNumberBlockedFlag.remove(fromAccountNumber);
                accountNumberBlockedFlag.remove(toAccountNumber);
            }
        }
    }
}
