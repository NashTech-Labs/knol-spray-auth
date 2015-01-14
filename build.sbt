name := "knol-spray-auth"

version := "1.0"

scalaVersion := "2.11.4"

resolvers += "spray repo" at "http://repo.spray.io"

resolvers += "spray nightlies" at "http://nightlies.spray.io"

val sprayV = "1.3.1"

val akkaV = "2.3.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka"  %% "akka-actor"       % akkaV,
  "com.typesafe.akka"  %% "akka-slf4j"       % akkaV,
  //"com.typesafe.akka"  %% "akka-testkit"     % akkaV        	% 	"test",
  "ch.qos.logback"      % "logback-classic"  % "1.0.13",
  "io.spray"            %% "spray-can"        % sprayV,
  "io.spray"            %% "spray-routing"    % sprayV,
  "io.spray"           %% "spray-json"       % sprayV,
  //"org.specs2"       %% "specs2"           % "2.11.0-SNAPSHOT"  	        % 	"test"
  "io.spray"            %% "spray-testkit"    % sprayV 		% 	"test",
  "org.scalatest"      %% "scalatest"        % "2.2.2" 		% 	"test"
 // "com.novocode"        % "junit-interface"  % "0.7"          	% 	"test->default"
)

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Ywarn-dead-code",
  "-language:_",
  "-target:jvm-1.7",
  "-encoding", "UTF-8"
)

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")
