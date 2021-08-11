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
    return source.getAccountId().compareTo(destination.id);
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
    synchronized (sourceLock) {
      log.info("Source account lock obtained");
      if (sourceAccount.getBalance().compareTo(amount) <= 0) {
        throw new IllegalArgumentException("Insufficient funds in source account with ID: " + sourceAccount.getAccountId());
      }
      sourceAccount.withdraw(amount);
      synchronized (destinationLock) {
        log.info("Destination account lock obtained");
        destinationAccount.deposit(amount);
      }
    }
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
