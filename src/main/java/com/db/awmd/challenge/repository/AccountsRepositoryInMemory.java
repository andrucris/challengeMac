package com.db.awmd.challenge.repository;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

  private final Map<String, Account> accounts = new ConcurrentHashMap<>();

  @Override
  public void createAccount(Account account) throws DuplicateAccountIdException {
    Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
    if (previousAccount != null) {
      throw new DuplicateAccountIdException(
        "Account id " + account.getAccountId() + " already exists!");
    }
  }

  private int compareAccountIds(Account source, Account destination) {
    return source.getAccountId().compareTo(destination.getAccountId());
  }
  @Override
  public void transfer(Account sourceAccount, Account destinationAccount, BigDecimal amount) {
    Account sourceLock, destinationLock;
    int result = compareAccountIds(sourceAccount,destinationAccount);
    //destination account id is greater than source account id
    if (result > 0) {
      sourceLock = destinationAccount;
      destinationLock = sourceAccount ;
    } else {
      sourceLock = sourceAccount;
      destinationLock = destinationAccount;
    }
    log.info("Source account balance value " + sourceAccount.getBalance().toString());
    log.info("Destination account balance value " + destinationAccount.getBalance().toString());
    synchronized (sourceLock) {
      log.info("Source account lock obtained");
      if (sourceAccount.getBalance().compareTo(amount) < 0) {
        throw new IllegalArgumentException("Insufficient funds in source account with ID: " + sourceAccount.getAccountId() + " available sum is " + sourceAccount.getBalance()+ " sum to be retrieved is " + amount);

      }
      sourceAccount.withdraw(amount);
      synchronized (destinationLock) {
        log.info("Destination account lock obtained");
        destinationAccount.deposit(amount);
      }
    }
    log.info("Source account balance value " + sourceAccount.getBalance().toString());
    log.info("Destination account balance value " + destinationAccount.getBalance().toString());
  }
  @Override
  public void transferWithoutSynchronization(Account sourceAccount, Account destinationAccount, BigDecimal amount) {

    log.info("Before Source account balance value " + sourceAccount.getBalance().toString());
    log.info("Before Destination account balance value " + destinationAccount.getBalance().toString());

    if (sourceAccount.getBalance().compareTo(amount) < 0) {
      throw new IllegalArgumentException("Insufficient funds in source account with ID: " + sourceAccount.getAccountId() + " available sum is " + sourceAccount.getBalance() + " sum to be retrieved is " + amount);
    }
    sourceAccount.withdraw(amount);
    destinationAccount.deposit(amount);
    log.info("After Source account balance value " + sourceAccount.getBalance().toString());
    log.info("After Destination account balance value " + destinationAccount.getBalance().toString());
  }
  @Override
  public Account getAccount(String accountId) {
    return accounts.get(accountId);
  }

  @Override
  public void clearAccounts() {
    accounts.clear();
  }

}
