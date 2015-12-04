name := "concurrent-programming-scala"

version := "1.0"

scalaVersion := "2.11.7"

fork := false

libraryDependencies += "org.scala-lang.modules" %% "scala-async" % "0.9.1"

libraryDependencies += "io.reactivex" %% "rxscala" % "0.25.0"

libraryDependencies += "org.scala-stm" %% "scala-stm" % "0.7"

libraryDependencies += "com.typesafe.akka" % "akka-actor_2.11" % "2.4.1"