package service;

import dao.AccountDaoImpl;
import exceptions.TransferException;
import model.Account;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import static org.junit.Assert.*;

public class AccountServiceImplTest {

    @Test
    public void transferMoney() {
        AccountService accountService = new AccountServiceImpl(new AccountDaoImpl());
        Account first = accountService.addAccount(new BigDecimal(200), new BigDecimal(0), Currency.getInstance("GBP"));
        Account second = accountService.addAccount(new BigDecimal(100), new BigDecimal(0), Currency.getInstance("GBP"));
        try {
            accountService.transferMoney(first.getAccountNumber(), second.getAccountNumber(), new BigDecimal(100));
            Account account = accountService.getAccount(first.getAccountNumber());
            Assert.assertTrue(account.getBalance().compareTo(new BigDecimal(100)) == 0);
        } catch (TransferException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void concurrentTransferDifferentAccount() throws InterruptedException {
        AccountService accountService = new AccountServiceImpl(new AccountDaoImpl());
        Account first = accountService.addAccount(new BigDecimal(200), new BigDecimal(0), Currency.getInstance("GBP"));
        Account second = accountService.addAccount(new BigDecimal(200), new BigDecimal(0), Currency.getInstance("GBP"));
        List<TransferMoneyThread> threads = new ArrayList<>();
        for (int i = 1; i < 100; i += 2) {
            threads.add(new TransferMoneyThread(1, accountService));
        }

        threads.forEach(Thread::start);
        for (Thread thread : threads) {
            thread.join();
        }

        Assert.assertTrue(threads.stream().filter(TransferMoneyThread::isError).findFirst().isPresent());
    }

    @Test
    public void concurrentTransferSameAccount() throws InterruptedException {
        AccountService accountService = new AccountServiceImpl(new AccountDaoImpl());
        Account first = accountService.addAccount(new BigDecimal(200), new BigDecimal(0), Currency.getInstance("GBP"));
        Account second = accountService.addAccount(new BigDecimal(200), new BigDecimal(0), Currency.getInstance("GBP"));
        List<TransferMoneyThread> threads = new ArrayList<>();
        for (int i = 0; i < 100; i += 2) {
            threads.add(new TransferMoneyThread(1, accountService));
        }

        threads.forEach(Thread::start);
        for (Thread thread : threads) {
            thread.join();
        }

        Assert.assertTrue(threads.stream().filter(TransferMoneyThread::isError).findFirst().isPresent());
    }

    @Test(expected = TransferException.class)
    public void invalidAmount() throws TransferException {
        AccountService accountService = new AccountServiceImpl(new AccountDaoImpl());
        Account first = accountService.addAccount(new BigDecimal(50), new BigDecimal(0), Currency.getInstance("GBP"));
        Account second = accountService.addAccount(new BigDecimal(100), new BigDecimal(0), Currency.getInstance("GBP"));
        accountService.transferMoney(first.getAccountNumber(), second.getAccountNumber(), new BigDecimal(100));
    }

    @Test(expected = TransferException.class)
    public void invalidAccount() throws TransferException {
        AccountService accountService = new AccountServiceImpl(new AccountDaoImpl());
        Account first = accountService.addAccount(new BigDecimal(100), new BigDecimal(0), Currency.getInstance("GBP"));
        Account second = accountService.addAccount(new BigDecimal(100), new BigDecimal(0), Currency.getInstance("GBP"));
        second.setAccountNumber(Long.MAX_VALUE);
        accountService.transferMoney(first.getAccountNumber(), second.getAccountNumber(), new BigDecimal(100));
    }

    @Test(expected = TransferException.class)
    public void sameAccountTransfer() throws TransferException {
        AccountService accountService = new AccountServiceImpl(new AccountDaoImpl());
        Account first = accountService.addAccount(new BigDecimal(100), new BigDecimal(0), Currency.getInstance("GBP"));
        Account second = accountService.addAccount(new BigDecimal(100), new BigDecimal(0), Currency.getInstance("GBP"));
        second.setAccountNumber(first.getAccountNumber());
        accountService.transferMoney(first.getAccountNumber(), second.getAccountNumber(), new BigDecimal(100));
    }

    @Test
    public void useOverdraft() throws TransferException {
        AccountService accountService = new AccountServiceImpl(new AccountDaoImpl());
        Account first = accountService.addAccount(new BigDecimal(50), new BigDecimal(50), Currency.getInstance("GBP"));
        Account second = accountService.addAccount(new BigDecimal(0), new BigDecimal(0), Currency.getInstance("GBP"));
        try {
            accountService.transferMoney(first.getAccountNumber(), second.getAccountNumber(), new BigDecimal(100));
            Account account = accountService.getAccount(second.getAccountNumber());
            Assert.assertTrue(account.getBalance().compareTo(new BigDecimal(100)) == 0);
        } catch (TransferException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test(expected = TransferException.class)
    public void insufficientFunds() throws TransferException {
        AccountService accountService = new AccountServiceImpl(new AccountDaoImpl());
        Account first = accountService.addAccount(new BigDecimal(50), new BigDecimal(50), Currency.getInstance("GBP"));
        Account second = accountService.addAccount(new BigDecimal(0), new BigDecimal(0), Currency.getInstance("GBP"));

        accountService.transferMoney(first.getAccountNumber(), second.getAccountNumber(), new BigDecimal(150));
        Account account = accountService.getAccount(second.getAccountNumber());

    }

    @Test
    public void editAccount() throws TransferException {
        AccountService accountService = new AccountServiceImpl(new AccountDaoImpl());
        Account first = accountService.addAccount(new BigDecimal(100), new BigDecimal(0), Currency.getInstance("GBP"));
        first.setBalance(new BigDecimal(200));
        first.setCurrency(Currency.getInstance("EUR"));
        first.setOverdraftLimit(new BigDecimal(300));
        boolean edited = accountService.editAccount(first);
        Account editedAccount = accountService.getAccount(first.getAccountNumber());
        Assert.assertTrue(edited);
        Assert.assertEquals(first.getBalance(), editedAccount.getBalance());
        Assert.assertEquals(first.getOverdraftLimit(), editedAccount.getOverdraftLimit());
        Assert.assertEquals(first.getCurrency(), editedAccount.getCurrency());

    }

    @Test(expected = TransferException.class)
    public void deleteAccount() throws TransferException{
        AccountService accountService = new AccountServiceImpl(new AccountDaoImpl());
        Account first = accountService.addAccount(new BigDecimal(100), new BigDecimal(0), Currency.getInstance("GBP"));

        boolean deleted = accountService.deleteAccount(first.getAccountNumber());

        Account checkDeleted = accountService.getAccount(first.getAccountNumber());
        Assert.assertTrue(deleted);


    }

    @Test
    public void accountExists() {
        AccountService accountService = new AccountServiceImpl(new AccountDaoImpl());
        Account first = accountService.addAccount(new BigDecimal(100), new BigDecimal(0), Currency.getInstance("GBP"));
        boolean exists = accountService.accountExists(first.getAccountNumber());
        Assert.assertTrue(exists);
    }

    private class TransferMoneyThread extends Thread {

        private int startIndex;
        private final AccountService accountService;
        private volatile boolean error;

        TransferMoneyThread(int startIndex, AccountService accountService) {
            this.startIndex = startIndex;
            this.accountService = accountService;
        }

        @Override
        public void run() {
            try {
                accountService.transferMoney(startIndex, startIndex + 1, new BigDecimal(100));
                error = true;
            } catch (IllegalArgumentException | TransferException ignored) {

            }
        }

        boolean isError() {
            return error;
        }
    }
}