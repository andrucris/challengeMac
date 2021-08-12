package com.db.awmd.challenge.web;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.service.AccountsService;
import javax.validation.Valid;

import com.db.awmd.challenge.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

  private final AccountsService accountsService;
  private final NotificationService notificationService;

  @Autowired
  public AccountsController(AccountsService accountsService, NotificationService notificationService) {
    this.accountsService = accountsService;
    this.notificationService = notificationService;
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
    log.info("Creating account {}", account);

    try {
    this.accountsService.createAccount(account);
    } catch (DuplicateAccountIdException daie) {
      return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping(path = "/{accountId}")
  public Account getAccount(@PathVariable String accountId) {
    log.info("Retrieving account for id {}", accountId);
    return this.accountsService.getAccount(accountId);
  }
  @PostMapping(path = "/{sourceAccountId}/{destAccountId}")
  public ResponseEntity<String> transfer(@PathVariable String sourceAccountId,
                                           @PathVariable String destAccountId,
                                           @RequestParam(required = true) String amount) {
    log.info("Transferring between accounts from account with id  {} to account with id {}", sourceAccountId, destAccountId);
    Account sourceAccount = this.accountsService.getAccount(sourceAccountId);
    Account destAccount = this.accountsService.getAccount(destAccountId);
    notificationService.notifyAboutTransfer(sourceAccount," transfer will be done form source account with id " + sourceAccountId + " to destination " +
            "account id " + destAccount.getAccountId() + " with amount  " + amount);
    this.accountsService.transferMoney(sourceAccount,destAccount,new BigDecimal(amount));
    return new ResponseEntity<String>(HttpStatus.OK);
  }
}
