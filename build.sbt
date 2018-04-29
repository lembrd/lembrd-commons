lazy val slf4jVersion = "1.7.25"
lazy val akkaVersion = "2.5.9"
lazy val prometheusVersion = "0.2.0"

val jodaTime = "joda-time" % "joda-time" % "2.9.4"

val typesafeConfigs = Seq(
  "com.github.kxbmap" %% "configs" % "0.4.4"
)

val guava = "com.google.guava" % "guava" % "22.0"
val sguice = "net.codingwell" %% "scala-guice" % "4.1.1"

lazy val loggingLibs = Seq(
  "org.slf4j" % "slf4j-api" % slf4jVersion,
  "org.slf4j" % "jcl-over-slf4j" % slf4jVersion,
  "org.slf4j" % "log4j-over-slf4j" % slf4jVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"
)

lazy val metrics = Seq(
  "io.dropwizard.metrics" % "metrics-core" % "4.0.1",
  "io.dropwizard.metrics" % "metrics-jvm" % "4.0.1",
  "nl.grons" %% "metrics-scala" % "4.0.0"
)

lazy val commonLibs =
  loggingLibs ++
    metrics ++
    Seq(
      "net.sf.trove4j" % "trove4j" % "3.0.3",
      "commons-lang" % "commons-lang" % "2.6",
      "com.univocity" % "univocity-parsers" % "2.6.3",
      "org.scala-lang.modules" %% "scala-java8-compat" % "0.8.0",
      "org.scalatest" %% "scalatest" % "3.0.0" % Test,
      jodaTime
    )

val sharedSettings = Seq(
  version := "1.0",
  organization := "com.lembrd",
  scalaVersion := "2.12.4",
  ivyXML :=
    <dependencies>
      <exclude org="com.sun.jmx" module="jmxri"/>
      <exclude org="com.sun.jdmk" module="jmxtools"/>
      <exclude org="javax.jms" module="jms"/>
      <exclude org="org.slf4j" module="slf4j-log4j12"/>
    </dependencies>,
  scalacOptions := Seq(
    // Note: Add -deprecation when deprecated methods are removed
    "-target:jvm-1.8",
    "-unchecked",
    "-feature",
    "-language:_",
    "-encoding", "utf8",
    "-Xlint:-missing-interpolator",
    "-Ypatmat-exhaust-depth", "40",
    "-Ypartial-unification"
  ),
  publishArtifact in(Compile, packageDoc) := false,
  publishArtifact in(Compile, packageSrc) := false,
  javacOptions ++= Seq("-Xlint:unchecked", "-source", "1.8", "-target", "1.8"),
  javacOptions in doc := Seq("-source", "1.8"),
  parallelExecution in Test := false,
  publishArtifact in Test := false,
  testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-n", "auto"),
  pomIncludeRepository := { _ => false },
  publishMavenStyle := true,
  libraryDependencies ++= commonLibs,

  dependencyOverrides += "ch.qos.logback" % "logback-classic" % "1.2.3",
  dependencyOverrides += "io.dropwizard.metrics" % "metrics-core" % "4.0.1",

  licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
)


lazy val projectList: Seq[sbt.ProjectReference] = Seq[sbt.ProjectReference](
  `lembrd-utils`,
  `lembrd-container`,
  `lembrd-akka`
)

lazy val `lembrd-commons` = Project(
  id = "lembrd-commons",
  base = file(".")
).settings(
  sharedSettings
).settings(
  publish := {}
).aggregate(projectList: _*)


lazy val `lembrd-utils` = Project(
  id = "lembrd-utils",
  base = file("lembrd-utils")
).settings(
  sharedSettings
).settings(
  name := "lembrd-utils"
)

lazy val `lembrd-container` = Project(
  id = "lembrd-container",
  base = file("lembrd-container")
).settings(
  sharedSettings
).settings(
  name := "lembrd-container",
  libraryDependencies ++= typesafeConfigs,
  libraryDependencies ++= Seq(
    sguice,
    "io.prometheus" % "simpleclient_dropwizard" % prometheusVersion,
    "io.prometheus" % "simpleclient_pushgateway" % prometheusVersion
  )
).dependsOn(
  `lembrd-utils`
)

lazy val `lembrd-akka` = Project(
  id = "lembrd-akka",
  base = file("lembrd-akka")
).settings(
  sharedSettings
).settings(
  name := "lembrd-akka",
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
  )
).dependsOn(
  `lembrd-container`,
  `lembrd-utils`
)
