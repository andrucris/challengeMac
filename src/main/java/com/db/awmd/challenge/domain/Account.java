package com.db.awmd.challenge.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

@Data
public class Account implements BankAccount{

  public static final String LOCK  = "Lock";
  @NotNull
  @NotEmpty
  private final String accountId;

  @NotNull
  @Min(value = 0, message = "Initial balance must be positive.")
  private BigDecimal balance;

  public Account(String accountId) {
    this.accountId = accountId;
    this.balance = BigDecimal.ZERO;
  }

  @JsonCreator
  public Account(@JsonProperty("accountId") String accountId,
    @JsonProperty("balance") BigDecimal balance) {
    this.accountId = accountId;
    this.balance = balance;
  }

  public String getAccountId() {
    return accountId;
  }

  public BigDecimal getBalance() {
    return balance;
  }

  public Account setBalance(BigDecimal balance) {
    this.balance = balance;
    return this;
  }

  @Override
  public void withdraw(BigDecimal value) {
    if(this.balance.subtract(value).compareTo(BigDecimal.ZERO)<0){
      return;
    }
    try {
      Thread.sleep(1000L); //simulating DB access
    } catch(InterruptedException e) {}
    setBalance(this.balance.subtract(value));
  }

  @Override
  public void deposit(BigDecimal value) {
    try {
      Thread.sleep(1000L); //simulating DB access
    } catch(InterruptedException e) {}
    setBalance(this.balance.add(value));
  }
}
