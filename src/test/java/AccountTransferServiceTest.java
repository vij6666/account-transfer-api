import com.google.gson.Gson;
import exceptions.ErrorCode;
import model.Account;
import model.TransferRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import org.apache.http.client.HttpClient;
import service.StandardResponse;
import service.StatusResponse;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Currency;

public class AccountTransferServiceTest {

    private AccountTransferService accountTransferService;

    @Before
    public void setUp(){
        accountTransferService = new AccountTransferService();
        accountTransferService.main(new String[0]);
    }

    @Test
    public void createAccount() throws Exception {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost postRequest = new HttpPost("http://localhost:4567" + "/accounts");
        postRequest.setHeader("Content-type", "application/json");

        Account account = new Account();
        account.setBalance(new BigDecimal(100));
        account.setOverdraftLimit(new BigDecimal(0));
        account.setCurrency(Currency.getInstance("GBP"));


        String addAccountJson = new Gson().toJson(account);
        postRequest.setEntity(new StringEntity(addAccountJson));

        HttpResponse response = httpClient.execute(postRequest);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK);
        String responseJson = EntityUtils.toString(response.getEntity(), "UTF-8");

        Assert.assertEquals("{\"status\":\"SUCCESS\"}", responseJson);


    }

    // ToDo - add tests for Get and Put methods
    @Test
    public void transferMoney() throws Exception {
        HttpClient httpClient1 = HttpClientBuilder.create().build();
        HttpPost postRequest1 = new HttpPost("http://localhost:4567" + "/accounts");
        postRequest1.setHeader("Content-type", "application/json");

        Account account1 = new Account();
        account1.setBalance(new BigDecimal(100));
        account1.setOverdraftLimit(new BigDecimal(0));
        account1.setCurrency(Currency.getInstance("GBP"));
        String addAccount1Json = new Gson().toJson(account1);
        postRequest1.setEntity(new StringEntity(addAccount1Json));
        HttpResponse response1 = httpClient1.execute(postRequest1);
        Assert.assertEquals(response1.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK);
        String response1Json = EntityUtils.toString(response1.getEntity(), "UTF-8");
        Assert.assertEquals("{\"status\":\"SUCCESS\"}", response1Json);

        Account account2 = new Account();
        account2.setBalance(new BigDecimal(100));
        account2.setOverdraftLimit(new BigDecimal(0));
        account2.setCurrency(Currency.getInstance("GBP"));
        HttpClient httpClient2 = HttpClientBuilder.create().build();
        HttpPost postRequest2 = new HttpPost("http://localhost:4567" + "/accounts");
        postRequest1.setHeader("Content-type", "application/json");
        String addAccount2Json = new Gson().toJson(account2);
        postRequest2.setEntity(new StringEntity(addAccount2Json));
        HttpResponse response2 = httpClient2.execute(postRequest2);
        Assert.assertEquals(response2.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK);
        String response2Json = EntityUtils.toString(response2.getEntity(), "UTF-8");

        Assert.assertEquals("{\"status\":\"SUCCESS\"}", response2Json);

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromAccountNumber(new Long(1));
        transferRequest.setToAccountNumber(new Long(2));
        transferRequest.setTransferAmount(new BigDecimal(100));

        HttpClient httpClient3 = HttpClientBuilder.create().build();
        HttpPost postRequest3 = new HttpPost("http://localhost:4567" + "/accounts/transfer");
        postRequest3.setHeader("Content-type", "application/json");
        String transferRequestJson = new Gson().toJson(transferRequest);
        postRequest3.setEntity(new StringEntity(transferRequestJson));
        HttpResponse response3 = httpClient2.execute(postRequest3);
        Assert.assertEquals(response3.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK);
        String response3Json = EntityUtils.toString(response3.getEntity(), "UTF-8");
        Assert.assertEquals("{\"status\":\"SUCCESS\",\"message\":\"Transfer Successful\"}", response3Json);

    }

    @After
    public void tearDown(){

    }
}