lazy val commonSettings = Seq(
  organization := "org.tresql",
  scalaVersion := "2.12.1",
  crossScalaVersions := Seq(
    "2.12.1",
    "2.11.8"
  ),
  //coverageEnabled := true,
  scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-language:dynamics",
    "-language:postfixOps", "-language:implicitConversions", "-language:reflectiveCalls",
    "-language:existentials",
    //"-verbose",
    "-Xmacro-settings:metadataFactoryClass=org.tresql.compiling.CompilerJDBCMetaData, driverClass=org.hsqldb.jdbc.JDBCDriver, url=jdbc:hsqldb:mem:., dbCreateScript=src/test/resources/db.sql, functions=org.tresql.compiling.Functions"),
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (version.value.trim.endsWith("SNAPSHOT"))
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
  publishArtifact in Compile := false,
  publishMavenStyle := true,
  sources in (Compile, doc) := Seq.empty
)

lazy val core = (project in file("core"))
  .settings(
    name := "tresql-core",
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4"
    )
  ).settings(commonSettings: _*)

lazy val macros = (project in file("macro"))
  .dependsOn(core)
  .settings(
    name := "macro"
  )
  .settings(commonSettings: _*)

val packageScopes = Seq(packageBin, packageSrc)

val packageProjects = Seq(core, macros)

val packageMerges = for {
  project <- packageProjects
  scope <- packageScopes
} yield mappings in(Compile, scope) := (mappings in (Compile, scope)).value ++ (mappings in (project, Compile, scope)).value


lazy val tresql = (project in file("."))
  .dependsOn(core, macros)
  .aggregate(core, macros)
  .settings(commonSettings: _*)
  .settings(packageMerges: _*)
  .settings(
    sources in (Compile, doc) := (sources in (core, Compile)).value ++ (sources in (macros, Compile)).value,

    name := "tresql",
    libraryDependencies ++= Seq("org.scalatest" %% "scalatest" % "3.0.0" % "test",
                                "org.hsqldb" % "hsqldb" % "2.3.1" % "test"),
    initialCommands in console := "import org.tresql._; import org.tresql.implicits._; import org.scalatest.run",
    publishArtifact in Test := false,
    publishArtifact in Compile := true,
    pomIncludeRepository := { x => false },
    pomExtra := (
      <url>https://github.com/uniso/tresql</url>
      <licenses>
        <license>
          <name>MIT</name>
          <url>http://www.opensource.org/licenses/MIT</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:uniso/tresql.git</url>
        <connection>scm:git:git@github.com:uniso/tresql.git</connection>
      </scm>
      <developers>
        <developer>
          <id>mrumkovskis</id>
          <name>Martins Rumkovskis</name>
          <url>https://github.com/mrumkovskis/</url>
        </developer>
        <developer>
          <id>guntiso</id>
          <name>Guntis Ozols</name>
          <url>https://github.com/guntiso/</url>
        </developer>
      </developers>
    )
  )
