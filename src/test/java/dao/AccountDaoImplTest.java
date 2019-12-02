package dao;

import model.Account;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;

public class AccountDaoImplTest {

    private AccountDaoImpl accountDao;


    @Before
    public void setUp() throws Exception{
        this.accountDao = new AccountDaoImpl();
    }

    @Test
    public void insertAccount() {
        BigDecimal balance = new BigDecimal(1000);
        BigDecimal overdraftLimit = new BigDecimal(0);
        Currency currency = Currency.getInstance("GBP");

        long accountNumber = accountDao.insertAccount(balance, overdraftLimit, currency);

        Assert.assertEquals(balance, accountDao.getAccount(accountNumber).getBalance());
        Assert.assertEquals(overdraftLimit, accountDao.getAccount(accountNumber).getOverdraftLimit());
        Assert.assertEquals(currency, accountDao.getAccount(accountNumber).getCurrency());
    }

    @Test
    public void deleteAccount() {
        BigDecimal balance = new BigDecimal(1000);
        BigDecimal overdraftLimit = new BigDecimal(0);
        Currency currency = Currency.getInstance("GBP");

        long accountNumber = accountDao.insertAccount(balance, overdraftLimit, currency);
        boolean deleted = accountDao.deleteAccount(accountNumber);
        Account account = accountDao.getAccount(accountNumber);
        Assert.assertEquals(true, deleted);
        Assert.assertNull(account);

    }




}