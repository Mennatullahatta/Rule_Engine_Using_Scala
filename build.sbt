name := "Rule Engine scala Project"
version := "0.1"
scalaVersion := "2.13.12"

//  dependencies
libraryDependencies ++= Seq(
  // CSV parsing
  "com.github.tototoshi" %% "scala-csv"      % "1.3.10",

  // Oracle database
  "com.oracle.database.jdbc" % "ojdbc11"     % "21.9.0.0",

  // for connection pooling
  "com.zaxxer"              % "HikariCP"     % "5.0.1",

  // JSON serialization
  "io.circe"               %% "circe-core"    % "0.14.6",
  "io.circe"               %% "circe-generic" % "0.14.6",
  "io.circe"               %% "circe-parser"  % "0.14.6",

  // Java 8 compatibility
  "org.scala-lang.modules" %% "scala-java8-compat" % "1.0.0"
)