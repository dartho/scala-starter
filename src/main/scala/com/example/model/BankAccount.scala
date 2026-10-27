package com.example.model

// Directory layout mirrors Java (src/main/scala/com/example/model), but unlike Java,
// Scala does not require it, and one file may hold several top-level types.

/** An enum (Scala 3). Compiles to something close to a Java enum. */
enum TransactionType {
  case Deposit, Withdrawal
}

/** A case class: an immutable data carrier. The compiler generates equals/hashCode/toString, a copy
  * method, and pattern-matching support. Roughly a Java `record`.
  */
final case class Transaction(kind: TransactionType, amount: BigDecimal)

/** A regular class. The parameter list after the name IS the primary constructor.
  *   - `val owner` becomes a public, read-only field with a getter.
  *   - `initialBalance` has no val/var, so it is just a constructor parameter.
  */
class BankAccount(val owner: String, initialBalance: BigDecimal) {

  // Constructor body: any statement in the class body runs at construction time.
  require(initialBalance >= 0, s"Initial balance must be non-negative, got $initialBalance")

  // Private mutable state. Idiomatic Scala prefers immutability, but this shows how `var` works.
  private var _balance: BigDecimal        = initialBalance
  private var _history: List[Transaction] = List.empty

  // Parameterless method: called as `account.balance`, no parentheses. Acts like a getter.
  def balance: BigDecimal = _balance

  def history: List[Transaction] = _history.reverse

  // Methods that change state conventionally keep their parentheses.
  def deposit(amount: BigDecimal): Unit = {
    require(amount > 0, "Deposit must be positive")
    _balance += amount
    _history = Transaction(TransactionType.Deposit, amount) :: _history
  }

  /** Returns Either instead of throwing: Left = error, Right = success. */
  def withdraw(amount: BigDecimal): Either[String, BigDecimal] =
    if (amount <= 0) Left("Withdrawal must be positive")
    else if (amount > _balance) Left(s"Insufficient funds: balance $_balance, requested $amount")
    else {
      _balance -= amount
      _history = Transaction(TransactionType.Withdrawal, amount) :: _history
      Right(_balance) // last expression is the return value; `return` is rarely used
    }

  override def toString: String = s"BankAccount($owner, balance=$_balance)"
}

/** Companion object: same name, same file. Holds what Java would mark `static`. */
object BankAccount {
  val MinimumOpeningDeposit: BigDecimal = BigDecimal(10)

  // Factory method. `BankAccount("Ana")` calls this because of the `apply` name.
  def apply(owner: String, initialBalance: BigDecimal = MinimumOpeningDeposit): BankAccount =
    new BankAccount(owner, initialBalance)
}
