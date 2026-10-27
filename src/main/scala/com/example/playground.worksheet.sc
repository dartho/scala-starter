// Metals worksheet: every line is evaluated on save, and results appear inline as comments.
// It sees the project classpath, so you can use classes from src/main/scala.
// sbt ignores *.sc files, so nothing here affects the build.
import com.example.model.BankAccount

val xs = List(3, 1, 2)
xs.sorted
xs.map(_ * 10).sum

val acct = BankAccount("Worksheet", 25)
acct.withdraw(5)
acct.withdraw(100)
