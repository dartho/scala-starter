// build.sbt -- the sbt equivalent of pom.xml / build.gradle.
// sbt reads every *.sbt file in the project root, plus *.scala/*.sbt files in project/.

ThisBuild / scalaVersion := "3.8.4"
ThisBuild / organization := "com.example"
ThisBuild / version      := "0.1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .settings(
    name := "scala-sandbox",

    // Compiler flags. -deprecation/-feature give you the "why" behind warnings,
    // -Wunused:all flags unused imports/vals (like IntelliJ's grey-out, but at compile time).
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-unchecked",
      "-Wunused:all"
    ),

    // Test framework. munit is lightweight and close to JUnit in feel.
    libraryDependencies += "org.scalameta" %% "munit" % "1.3.6" % Test,

    // Which main class `sbt run` uses when more than one object has a main method.
    Compile / run / mainClass := Some("com.example.Main"),

    // Run the app in a forked JVM so `sys.exit`, stdin, and JVM options behave normally.
    run / fork         := true,
    run / connectInput := true
  )
