package com.example

import com.example.model.{BankAccount, TransactionType}

/** Entry point. An `object` is a singleton; its `main` is the equivalent of
  * `public static void main(String[] args)`.
  */
object Main {
  def main(args: Array[String]): Unit = {
    val account = BankAccount("Ana", BigDecimal(100)) // uses the companion's apply, no `new`
    account.deposit(50) // Int 50 widens to BigDecimal implicitly

    // Pattern match on the Either returned by withdraw.
    account.withdraw(500) match {
      case Right(newBalance) => println(s"Withdrew; balance now $newBalance")
      case Left(error)       => println(s"Withdrawal refused: $error")
    }
    account.withdraw(30).foreach(b => println(s"Withdrew 30; balance now $b"))

    println(account)
    account.history.foreach { tx =>
      val sign = if (tx.kind == TransactionType.Deposit) "+" else "-"
      println(f"  $sign${tx.amount}%8.2f  ${tx.kind}")
    }
  }
}
