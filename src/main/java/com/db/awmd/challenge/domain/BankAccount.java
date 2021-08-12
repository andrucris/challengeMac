package com.db.awmd.challenge.domain;

import java.math.BigDecimal;

public interface BankAccount {

    void withdraw(BigDecimal value);

    void deposit(BigDecimal value) ;
}
