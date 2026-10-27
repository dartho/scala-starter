package com.example

import scala.util.{Failure, Success, Try}

import com.example.Scala3Defs.*

/** Scratch area for trying Scala features. Run it with `sbt "runMain com.example.Sandbox"` or the
  * "Run Sandbox" launch config. Each section is a method so you can comment calls in and out of
  * `main`.
  */
object Sandbox {

  def main(args: Array[String]): Unit = {
    section("vals, vars, type inference")(valsAndVars())
    section("strings")(strings())
    section("collections")(collections())
    section("Option instead of null")(options())
    section("pattern matching")(patternMatching())
    section("functions")(functions())
    section("for-comprehensions")(forComprehensions())
    section("error handling")(errorHandling())
    section("Java interop")(javaInterop())
    section("traits and mixins")(traitsAndMixins())
    section("extension methods")(extensionMethods())
    section("given / using")(givenAndUsing())
    section("opaque types")(opaqueTypes())
    section("Future-based concurrency")(futures())
  }

  // A by-name parameter (`=> Unit`): `body` is evaluated when used, not when passed.
  private def section(title: String)(body: => Unit): Unit = {
    println(s"\n=== $title ===")
    body
  }

  private def valsAndVars(): Unit = {
    val name         = "Scala" // like `final var name = "Scala"` in Java; type is inferred
    var counter: Int = 0       // mutable; explicit type annotation is optional
    counter += 1
    val answer = if (counter > 0) "positive" else "zero" // if is an expression, not a statement
    println(s"$name counter=$counter answer=$answer")
  }

  private def strings(): Unit = {
    val lang    = "Scala"
    val version = 3
    println(s"Hello, $lang ${version + 0}!")  // s-interpolator: like String.format but inline
    println(f"Pi is roughly ${math.Pi}%.3f")  // f-interpolator: printf-style, type-checked
    println(raw"No escape: \n stays literal") // raw-interpolator
    val multi =
      """|Triple-quoted strings
         |span lines; stripMargin trims
         |up to the | character.""".stripMargin
    println(multi)
  }

  private def collections(): Unit = {
    // Default collections are immutable. Operations return new collections.
    val nums  = List(1, 2, 3, 4, 5)
    val evens = nums.filter(_ % 2 == 0) // `_` is shorthand for a lambda parameter
    val sq    = nums.map(n => n * n)
    val total = nums.foldLeft(0)(_ + _) // like stream().reduce(0, Integer::sum)
    println(s"nums=$nums evens=$evens squares=$sq total=$total")

    val ages  = Map("Ana" -> 34, "Ben" -> 28) // `a -> b` builds a tuple (a, b)
    val older = ages.map { case (k, v) => k -> (v + 1) }
    println(s"ages=$ages older=$older ben=${ages.getOrElse("Ben", -1)}")

    val vec = Vector("a", "b", "c") // indexed, efficient random access
    println(s"vec(1)=${vec(1)} zipped=${vec.zipWithIndex}")

    val buf = scala.collection.mutable.ArrayBuffer(1, 2) // like ArrayList
    buf += 3
    println(s"mutable buffer=$buf grouped=${nums.groupBy(_ % 2 == 0)}")
  }

  private def options(): Unit = {
    def lookup(id: Int): Option[String] = if (id == 1) Some("Ana") else None

    println(lookup(1).map(_.toUpperCase).getOrElse("unknown"))
    println(lookup(2).map(_.toUpperCase).getOrElse("unknown"))
    // Wrap a possibly-null Java value:
    val fromJava: Option[String] = Option(System.getenv("NOT_A_REAL_VAR"))
    println(s"fromJava=$fromJava")
  }

  private def patternMatching(): Unit = {
    // Sealed: the compiler knows all subtypes and warns if a match misses one.
    sealed trait Shape
    final case class Circle(r: Double)          extends Shape
    final case class Rect(w: Double, h: Double) extends Shape

    def area(s: Shape): Double = s match {
      case Circle(r)            => math.Pi * r * r
      case Rect(w, h) if w == h => w * w // guard
      case Rect(w, h)           => w * h
    }

    List(Circle(1), Rect(2, 3), Rect(2, 2)).foreach(s => println(f"$s area=${area(s)}%.2f"))

    def describe(x: Any): String = x match {
      case 0               => "zero"
      case i: Int if i < 0 => "negative int"
      case s: String       => s"string of length ${s.length}"
      case (a, b)          => s"tuple of $a and $b"
      case head :: _       => s"list starting with $head"
      case _               => "something else"
    }
    List(0, -5, "hi", (1, "x"), List(9, 8), 3.14).foreach(x => println(describe(x)))
  }

  private def functions(): Unit = {
    val add: (Int, Int) => Int = (a, b) => a + b // function value; like BiFunction<Integer,...>
    def twice(f: Int => Int)(x: Int): Int = f(f(x))    // higher-order method, two parameter lists
    val addTen                            = add(10, _) // partial application
    println(s"add=${add(2, 3)} twice=${twice(_ * 3)(2)} addTen=${addTen(5)}")

    def greet(name: String, greeting: String = "Hello"): String = s"$greeting, $name"
    println(greet("Ana"))
    println(greet(greeting = "Hi", name = "Ben")) // named arguments, any order
  }

  private def forComprehensions(): Unit = {
    // for/yield works on any type with map/flatMap/filter: List, Option, Either, Future...
    val pairs = for {
      x <- 1 to 3
      y <- List("a", "b")
      if x != 2
    } yield s"$x$y"
    println(pairs)

    val sum = for {
      a <- Some(1)
      b <- Some(2)
    } yield a + b
    println(s"Option sum=$sum")
  }

  private def errorHandling(): Unit = {
    // Try captures exceptions as values.
    def parse(s: String): Try[Int] = Try(s.toInt)
    parse("42") match {
      case Success(n)  => println(s"parsed $n")
      case Failure(ex) => println(s"failed: ${ex.getMessage}")
    }
    println(parse("abc").getOrElse(-1))

    // Either: typed errors without exceptions. Right-biased, so map/flatMap act on Right.
    def safeDiv(a: Int, b: Int): Either[String, Int] =
      if (b == 0) Left("divide by zero") else Right(a / b)
    println(s"${safeDiv(10, 2)} ${safeDiv(1, 0)}")

    // Plain try/catch still exists; catch uses pattern matching.
    try Integer.parseInt("x")
    catch { case e: NumberFormatException => println(s"caught ${e.getClass.getSimpleName}") }
  }

  private def javaInterop(): Unit = {
    import scala.jdk.CollectionConverters.* // asScala / asJava

    val javaList = new java.util.ArrayList[String]()
    javaList.add("from")
    javaList.add("java")
    val scalaList = javaList.asScala.toList
    println(s"scala list: ${scalaList.mkString(" ")}")

    val now = java.time.LocalDate.now() // any Java class is usable directly
    println(s"today is ${now.getDayOfWeek}")
  }

  private def traitsAndMixins(): Unit = {
    val r2 = Robot("R2")
    r2.log("booted")
    r2.log("greeted")
    println(r2.greet())
    println(s"log=${r2.logged}") // state that lives in the Logging trait

    // Mix a trait in at instantiation: an anonymous subclass of Robot that also extends Loud.
    val c3 = new Robot("C3") with Loud
    println(c3.greet()) // Loud.greet calls super.greet, which resolves to Greeter.greet

    // A trait is a type, so it works for polymorphism just like a Java interface.
    val greeters: List[Greeter] = List(r2, c3)
    println(greeters.map(_.name))
  }

  private def extensionMethods(): Unit = {
    // These methods are not on String or List; they come from Scala3Defs via the import above.
    println(s"'Never odd or even'.isPalindrome = ${"Never odd or even".isPalindrome}")
    println(s"'  split   these words '.words = ${"  split   these words ".words}")
    println(s"List(1, 2, 3).second = ${List(1, 2, 3).second}, List(1).second = ${List(1).second}")
  }

  private def givenAndUsing(): Unit = {
    // The compiler finds the Show[Int] instance in Show's companion object automatically.
    println(describe(42))
    // Show[List[Int]] is built from the generic given, which itself needs a Show[Int].
    println(describe(List(1, 2, 3)))
    println(describeAll(List(7, 8)))
    println(5.shown) // extension method that requires a given
    // describe(3.14)  // does not compile: "No given instance of type Show[Double] was found"

    // `using` for context passing: a value that would otherwise be threaded through every call.
    given Config = Config(appName = "sandbox", verbose = true)
    println(logLine("starting"))
    println(logLine("explicit config")(using Config("other", verbose = false))) // pass explicitly
  }

  private def opaqueTypes(): Unit = {
    import Units.*
    val run   = Meters(5000)
    val total = run + Meters(195)
    println(s"total = ${total.value} m = ${total.toFeet.value} ft")
    // At runtime these are plain Doubles: no wrapper object is allocated.
    // val bad: Double = total        // does not compile: Meters is not a Double outside Units
    // run + Feet(10)                 // does not compile: Feet is not Meters
  }

  private def futures(): Unit = {
    import scala.concurrent.{Await, Future}
    import scala.concurrent.duration.*
    // The thread pool Futures run on. It is an implicit value, so Future.apply/map/flatMap
    // pick it up automatically. In real code you would usually pass in a dedicated one.
    import scala.concurrent.ExecutionContext.Implicits.global

    def slowSquare(n: Int): Future[Int] = Future {
      Thread.sleep(200) // simulate I/O
      n * n
    }

    def timed[A](label: String)(f: => A): A = {
      val start = System.nanoTime()
      val a     = f
      println(f"$label%-28s -> $a (${(System.nanoTime() - start) / 1e6}%.0f ms)")
      a
    }

    // A Future starts running when it is created. Create both first, then combine: parallel.
    timed("parallel for-comprehension") {
      val fa = slowSquare(3)
      val fb = slowSquare(4)
      Await.result(for { a <- fa; b <- fb } yield a + b, 2.seconds)
    }

    // Pitfall: creating them inside the for-comprehension makes the second wait for the first.
    timed("sequential for-comprehension") {
      Await.result(for { a <- slowSquare(3); b <- slowSquare(4) } yield a + b, 2.seconds)
    }

    // List[Future[Int]] -> Future[List[Int]], like CompletableFuture.allOf + join.
    timed("Future.sequence of 5") {
      Await.result(Future.sequence((1 to 5).toList.map(slowSquare)), 2.seconds)
    }

    // Failures travel inside the Future; recover is like CompletableFuture.exceptionally.
    timed("recover from failure") {
      val failing = Future(10 / 0).recover { case _: ArithmeticException => -1 }
      Await.result(failing, 2.seconds)
    }
    // Await blocks a thread. Use it only at the edge of the program (main, tests), never
    // inside Future code; there, compose with map/flatMap instead.
  }
}

/** Definitions used by the Scala 3 sections of Sandbox. Opaque types must be declared in an object
  * or at the top level, not inside a method, so these live here. A single .scala file may hold
  * several top-level objects, classes and traits.
  */
object Scala3Defs {

  // ---------- traits and mixins ----------

  trait Greeter {
    def name: String // abstract member
    def greet(): String = s"Hello from $name" // concrete method, like a Java default method
  }

  trait Logging {
    private var entries = Vector.empty[String] // traits can hold state; Java interfaces cannot
    def log(msg: String): Unit = entries :+= msg
    def logged: Vector[String] = entries
  }

  // A trait that extends Greeter and modifies its behaviour ("stackable modification").
  trait Loud extends Greeter {
    override def greet(): String = super.greet().toUpperCase + "!"
  }

  // `extends A with B`: one superclass or trait first, then any number of traits.
  class Robot(val name: String) extends Greeter with Logging

  // ---------- extension methods ----------

  extension (s: String) {
    def isPalindrome: Boolean = {
      val letters = s.toLowerCase.filter(_.isLetter)
      letters == letters.reverse
    }
    def words: List[String] = s.split("\\s+").toList.filter(_.nonEmpty)
  }

  extension [A](xs: List[A]) {
    def second: Option[A] = xs.drop(1).headOption
  }

  // ---------- given / using ----------

  /** A type class: behaviour for a type, defined outside that type. */
  trait Show[A] {
    def show(a: A): String
  }

  object Show {
    // Givens in the companion object are found without any import.
    given Show[Int]    = (a: Int) => s"Int($a)"
    given Show[String] = (a: String) => s"\"$a\""

    // A given that depends on another given: Show[List[A]] exists whenever Show[A] does.
    given [A](using s: Show[A]): Show[List[A]] = (as: List[A]) =>
      as.map(s.show).mkString("[", ", ", "]")
  }

  def describe[A](a: A)(using s: Show[A]): String = s.show(a)

  // Context bound `A: Show` is shorthand for `(using Show[A])`; summon fetches the instance.
  def describeAll[A: Show](as: List[A]): String = as.map(summon[Show[A]].show).mkString(" | ")

  extension [A](a: A)(using s: Show[A]) {
    def shown: String = s.show(a)
  }

  final case class Config(appName: String, verbose: Boolean)

  def logLine(msg: String)(using cfg: Config): String =
    if (cfg.verbose) s"[${cfg.appName}] $msg" else msg

  // ---------- opaque types ----------

  object Units {
    // Inside Units, Meters IS Double. Outside, it is a distinct type with only the API below.
    opaque type Meters = Double
    opaque type Feet   = Double

    object Meters { def apply(d: Double): Meters = d }
    object Feet   { def apply(d: Double): Feet = d   }

    extension (m: Meters) {
      def value: Double            = m
      def +(other: Meters): Meters = m + other
      def toFeet: Feet             = m * 3.28084
    }

    extension (f: Feet) {
      @annotation.targetName(
        "feetValue"
      ) // both `value` methods would erase to the same JVM signature
      def value: Double = f
    }
  }
}
