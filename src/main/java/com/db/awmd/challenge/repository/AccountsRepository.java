package com.db.awmd.challenge.repository;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;

import java.math.BigDecimal;

public interface AccountsRepository {

  void createAccount(Account account) throws DuplicateAccountIdException;

  Account getAccount(String accountId);

  void clearAccounts();
  void transfer(Account acc1, Account acc2, BigDecimal value);
  void transferWithoutSynchronization(Account sourceAccount, Account destinationAccount, BigDecimal amount);
}
