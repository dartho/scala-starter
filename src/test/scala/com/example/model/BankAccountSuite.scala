package com.example.model

class BankAccountSuite extends munit.FunSuite {

  test("deposit increases balance") {
    val acct = BankAccount("Ana", 100)
    acct.deposit(50)
    assertEquals(acct.balance, BigDecimal(150))
  }

  test("withdraw more than balance returns Left and leaves balance unchanged") {
    val acct = BankAccount("Ana", 20)
    assert(acct.withdraw(50).isLeft)
    assertEquals(acct.balance, BigDecimal(20))
  }

  test("default opening deposit comes from the companion object") {
    assertEquals(BankAccount("Ben").balance, BankAccount.MinimumOpeningDeposit)
  }

  test("negative initial balance is rejected") {
    intercept[IllegalArgumentException](new BankAccount("Bad", -1))
  }
}
