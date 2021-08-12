package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.test.util.AssertionErrors.fail;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.service.AccountsService;
import java.math.BigDecimal;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceTest {

  @Autowired
  private AccountsService accountsService;

  @Test
  public void addAccount() throws Exception {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  public void addAccount_failsOnDuplicateId() throws Exception {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);

    try {
      this.accountsService.createAccount(account);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }

  }
  @Test
  public void transferInsufficientFunds(){
    String sourceUniqueId = "Id-" + System.currentTimeMillis();
    Account sourceAccount = new Account(sourceUniqueId);

    sourceAccount.deposit(new BigDecimal(100));

    String destinationUniqueId = "Id-" + System.currentTimeMillis();
    Account destinationAccount = new Account(destinationUniqueId);

    try {
      this.accountsService.transferMoney(sourceAccount, destinationAccount, new BigDecimal(500));
    }catch (IllegalArgumentException e){
      assertThat(e.getMessage().startsWith("Insufficient funds in source account with ID: " )).isTrue();
    }

  }
  @Test
  public void transferFunds(){
    String sourceUniqueId = "Id-" + System.currentTimeMillis();
    Account sourceAccount = new Account(sourceUniqueId);


    String destinationUniqueId = "Id-" + System.currentTimeMillis();
    Account destinationAccount = new Account(destinationUniqueId);


    sourceAccount.deposit(new BigDecimal(1000));

    this.accountsService.transferMoney(sourceAccount, destinationAccount, new BigDecimal(500));
    assertThat(sourceAccount.getBalance()).isEqualTo(new BigDecimal(500));
    assertThat(destinationAccount.getBalance()).isEqualTo(new BigDecimal(500));


  }
  @Test
  public void testDeadLock(){


    String sourceUniqueId = "Id-" + System.currentTimeMillis();
    Account sourceAccount = new Account(sourceUniqueId);



    String destinationUniqueId = "Id-" + System.currentTimeMillis();
    Account destinationAccount = new Account(destinationUniqueId);


    sourceAccount.deposit(new BigDecimal(100));
    destinationAccount.deposit(new BigDecimal(80));

    try {
    Thread t1 = new Thread(new Runnable() {
      @Override
      public void run() {
        System.out.println("Inside : " + Thread.currentThread().getName());
        accountsService.transferMoney(sourceAccount,destinationAccount,new BigDecimal(100));
      }
    });
    Thread t2 = new Thread(new Runnable() {
      @Override
      public void run() {
        System.out.println("Inside : " + Thread.currentThread().getName());
        accountsService.transferMoney(sourceAccount,destinationAccount,new BigDecimal(100));
      }
    });
    Thread t3 = new Thread(new Runnable() {
      @Override
      public void run() {
        System.out.println("Inside : " + Thread.currentThread().getName());
        accountsService.transferMoney(sourceAccount,destinationAccount,new BigDecimal(80));
      }
    });

    t1.start();
    t2.start();
    t3.start();

    try {
      t1.join();
      t2.join();
      t3.join();
      } catch(InterruptedException e){}

    }catch (IllegalArgumentException e) {
      assertThat(e.getMessage().startsWith("Insufficient funds in source account with ID: ")).isTrue();
    }
  }

  @Test
  public void testDeadLockWithoutSynchronization(){


    String sourceUniqueId = "Id-" + System.currentTimeMillis();
    Account sourceAccount = new Account(sourceUniqueId);



    String destinationUniqueId = "Id-" + System.currentTimeMillis();
    Account destinationAccount = new Account(destinationUniqueId);


    sourceAccount.deposit(new BigDecimal(100));
    destinationAccount.deposit(new BigDecimal(80));
    Thread t1 = new Thread(new Runnable() {
      @Override
      public void run() {
        System.out.println("Inside : " + Thread.currentThread().getName());
       accountsService.transferMoneyWithoutSynchronization(sourceAccount,destinationAccount,new BigDecimal(100));
      }
    });
    Thread t2 = new Thread(new Runnable() {
      @Override
      public void run() {
        System.out.println("Inside : " + Thread.currentThread().getName());
        accountsService.transferMoneyWithoutSynchronization(sourceAccount,destinationAccount,new BigDecimal(100));
      }
    });
    Thread t3 = new Thread(new Runnable() {
      @Override
      public void run() {
        System.out.println("Inside : " + Thread.currentThread().getName());
        accountsService.transferMoneyWithoutSynchronization(sourceAccount,destinationAccount,new BigDecimal(5));
      }
    });

    t1.start();
    t2.start();
    t3.start();

    try {
      t1.join();
      t2.join();
      t3.join();
    } catch(InterruptedException e){}
    assertThat(sourceAccount.getBalance().compareTo(BigDecimal.ZERO)<0);


  }
}
