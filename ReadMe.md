Build with :

``````
./gradlew clean build -x test bootRun
```````



Transfer money between accounts - analyze for multithreading - see AccountsServiceTest.java#testDeadLockWithoutSynchronization
and method accountService#transferMoneyWithoutSynchronization

The shared variable is the balance of the source account. We need lock in order to not use the same variable. 

see logs
Inside : Thread-4
Inside : Thread-6
Inside : Thread-5
2021-08-13 00:17:02.320  INFO 63120 --- [       Thread-5] c.d.a.c.r.AccountsRepositoryInMemory     : Before Source account balance value 100
2021-08-13 00:17:02.319  INFO 63120 --- [       Thread-4] c.d.a.c.r.AccountsRepositoryInMemory     : Before Source account balance value 100
2021-08-13 00:17:02.319  INFO 63120 --- [       Thread-6] c.d.a.c.r.AccountsRepositoryInMemory     : Before Source account balance value 100
2021-08-13 00:17:02.322  INFO 63120 --- [       Thread-4] c.d.a.c.r.AccountsRepositoryInMemory     : Before Destination account balance value 80
2021-08-13 00:17:02.322  INFO 63120 --- [       Thread-5] c.d.a.c.r.AccountsRepositoryInMemory     : Before Destination account balance value 80
2021-08-13 00:17:02.323  INFO 63120 --- [       Thread-6] c.d.a.c.r.AccountsRepositoryInMemory     : Before Destination account balance value 80
2021-08-13 00:17:04.330  INFO 63120 --- [       Thread-5] c.d.a.c.r.AccountsRepositoryInMemory     : After Source account balance value -5
2021-08-13 00:17:04.330  INFO 63120 --- [       Thread-4] c.d.a.c.r.AccountsRepositoryInMemory     : After Source account balance value -5
2021-08-13 00:17:04.331  INFO 63120 --- [       Thread-5] c.d.a.c.r.AccountsRepositoryInMemory     : After Destination account balance value 280
2021-08-13 00:17:04.331  INFO 63120 --- [       Thread-4] c.d.a.c.r.AccountsRepositoryInMemory     : After Destination account balance value 280
2021-08-13 00:17:04.333  INFO 63120 --- [       Thread-6] c.d.a.c.r.AccountsRepositoryInMemory     : After Source account balance value -5
2021-08-13 00:17:04.333  INFO 63120 --- [       Thread-6] c.d.a.c.r.AccountsRepositoryInMemory     : After Destination account balance value 285


Scenario for transfer without lock :
Thread 5 : Source account with balance 100  transfers to Account destination $100
Thread 4 : Source account with balance 100  transfers to Account destination $100
Thread 6 : Source account with balance 100  transfers to Account destination $5
Thread 5 : Resumes and since it already passed the condition it will deduct the $100 from account source resulting negative amount
Thread 4 : Checks if source has balance >= amount, condition is true then it halts
Thread 5 : Resumes and since it already passed the condition it will add the $100 to account destination 



We need to add a lock to update the mutable object .

A solution could be to provide a global lock object and use account id to enforce the lock order that should solve the problem
