package model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {

    private long fromAccountNumber;
    private long toAccountNumber;
    private BigDecimal transferAmount;
}
