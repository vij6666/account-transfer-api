import dao.AccountDao;
import dao.AccountDaoImpl;
import exceptions.TransferException;
import model.Account;
import model.TransferRequest;
import service.AccountService;
import service.AccountServiceImpl;

import java.util.Currency;

import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.options;
import static spark.Spark.post;
import static spark.Spark.put;

import com.google.gson.Gson;
import service.StandardResponse;
import service.StatusResponse;

public class AccountTransferService {

    public static void main(String[] args) {

        final AccountService accountService = new AccountServiceImpl(new AccountDaoImpl());


        // Create an account using HTTP post method
        post("/accounts", (request, response) -> {
            response.type("application.json");
            Account account = new Gson().fromJson(request.body(), Account.class);
            accountService.addAccount(account.getBalance(), account.getOverdraftLimit(), Currency.getInstance(account.getCurrency().toString()));

            return new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS));
        });

        get("/accounts", (request, response) -> {
            response.type("application/json");
            return new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS, new Gson().toJsonTree(accountService.getAccounts())));

        });


        get("/accounts/:accountNumber", (request, response) -> {
            response.type("application.json");
            try {
                return new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS, new Gson().toJsonTree(accountService.getAccount(Long.parseLong(request.params(":accountNumber"))))));
            } catch (TransferException e){
                return new Gson().toJson(new StandardResponse(StatusResponse.ERROR, e.getMessage()));
            }
        });

        put("/accounts/:accountNumber", (request, response) -> {
            response.type("application/json");

            Account toEdit = new Gson().fromJson(request.body(), Account.class);
            boolean edited = accountService.editAccount(toEdit);

            if (edited) {
                return new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS, "Account edited Successfully"));
            } else {
                return new Gson().toJson(new StandardResponse(StatusResponse.ERROR, new Gson().toJson("Account not found or error in edit")));
            }
        });

        post("/accounts/transfer", (request, response) -> {
            response.type("application.json");

            TransferRequest transferRequest = new Gson().fromJson(request.body(), TransferRequest.class);
            try {
                accountService.transferMoney(transferRequest.getFromAccountNumber(), transferRequest.getToAccountNumber(), transferRequest.getTransferAmount());
                return new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS, "Transfer Successful"));
            } catch (TransferException e) {
                return new Gson().toJson(new StandardResponse(StatusResponse.ERROR, e.getMessage()));
            }
        });

        delete("/accounts/:accountNumber", (request, response) -> {
            response.type("application/json");
            try {
                accountService.deleteAccount(Long.parseLong(request.params(":accountNumber")));
                return new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS, "user deleted"));
            } catch (TransferException e) {
                return new Gson().toJson(new StandardResponse(StatusResponse.ERROR, e.getMessage()));
            }
        });

        options("/accounts/:accountNumber", (request, response) -> {
            response.type("application/json");

            return new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS, (accountService.accountExists(Long.parseLong(request.params(":accountNumber")))) ? "Account exists" : "Account does not exists"));
        });
    }

}
