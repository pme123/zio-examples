import macros.defaultScalaOpts
import mill._
import mill.scalalib._

trait MyModule extends ScalaModule {
  def scalaVersion = "2.13.1"

  object version {
    val cats = "2.0.0"
    val circe = "0.12.1"
    val circeYaml = "0.12.0"
    val doobie = "0.8.0-RC1"
    val http4s = "0.21.0-M4"
    val pureconfig = "0.12.1"
    val scalaXml = "1.2.0"
    val sttp = "1.6.3"
    val zio = "1.0.0-RC16"
    val zioMacros = "0.5.0"
    val zioCats = "2.0.0.0-RC7"
  }

  object libs {
    val cats = ivy"org.typelevel::cats-core:${version.cats}"
    val circeCore = ivy"io.circe::circe-core:${version.circe}"
    val circeGeneric = ivy"io.circe::circe-generic:${version.circe}"
    val circeYaml = ivy"io.circe::circe-yaml:${version.circeYaml}"
    val doobieCore = ivy"org.tpolecat::doobie-core:${version.doobie}"
    val doobieH2 = ivy"org.tpolecat::doobie-h2:${version.doobie}"
    val http4sBlazeServer =
      ivy"org.http4s::http4s-blaze-server:${version.http4s}"
    val http4sBlazeClient =
      ivy"org.http4s::http4s-blaze-client:${version.http4s}"
    val http4sCirce = ivy"org.http4s::http4s-circe:${version.http4s}"
    val http4sDsl = ivy"org.http4s::http4s-dsl:${version.http4s}"
    val pureconfig =
      ivy"com.github.pureconfig::pureconfig:${version.pureconfig}"
    val scalaXml = ivy"org.scala-lang.modules::scala-xml:${version.scalaXml}"
    val sttpCore = ivy"com.softwaremill.sttp::core:${version.sttp}"
    val sttpClient =
      ivy"com.softwaremill.sttp::async-http-client-backend-zio:${version.sttp}"
    val sttpCirce = ivy"com.softwaremill.sttp::circe::${version.sttp}"
    val zio = ivy"dev.zio::zio:${version.zio}"
    val zioMacrosAccess = ivy"dev.zio::zio-macros-core:${version.zioMacros}"
    //  val zioMacrosMockable = ivy"dev.zio::zio-macros-mock:${version.zioMacros}"
    val zioStream = ivy"dev.zio::zio-streams:${version.zio}"
    val zioCats = ivy"dev.zio::zio-interop-cats:${version.zioCats}"
  }

  object test extends Tests {
    override def ivyDeps = Agg(
      ivy"dev.zio::zio-test:${version.zio}",
      ivy"dev.zio::zio-test-sbt:${version.zio}"
    )

    def testOne(args: String*) = T.command {
      super.runMain("org.scalatest.run", args: _*)
    }

    def testFrameworks =
      Seq("zio.test.sbt.ZTestFramework")
  }

  override def scalacOptions =
    defaultScalaOpts

  val defaultScalaOpts = Seq(
    "-deprecation", // Emit warning and location for usages of deprecated APIs.
    "-encoding",
    "UTF-8", // Specify character encoding used by source files.
    "-language:higherKinds", // Allow higher-kinded types
    "-language:postfixOps", // Allows operator syntax in postfix position (deprecated since Scala 2.10)
    "-feature" // Emit warning and location for usages of features that should be imported explicitly.
    //  "-Ypartial-unification",      // Enable partial unification in type constructor inference
    //  "-Xfatal-warnings"            // Fail the compilation if there are any warnings
  )

}

object root extends MyModule {
  override def ivyDeps = {
    Agg(
      libs.cats,
      libs.circeCore,
      libs.circeGeneric,
      libs.doobieCore,
      libs.doobieH2,
      libs.http4sBlazeServer,
      libs.http4sCirce,
      libs.http4sDsl,
      libs.pureconfig,
      libs.sttpCore,
      libs.sttpClient,
      libs.sttpCirce,
      libs.zio,
      libs.zioStream,
      libs.zioCats
    )
  }
}

object macros extends MyModule {

  override def scalacOptions =
    defaultScalaOpts ++ Seq("-Ymacro-annotations", "-Ymacro-debug-lite")

  override def ivyDeps = {
    Agg(
      libs.zio,
      libs.zioMacrosAccess
      //    libs.zioMacrosMockable
    )
  }
}

object hocon extends MyModule {

  override def scalacOptions =
    defaultScalaOpts ++ Seq("-Ymacro-annotations", "-Ymacro-debug-lite")

  override def ivyDeps = {
    Agg(
      libs.zio,
      libs.pureconfig
    )
  }
}

object yaml extends MyModule {
  override def moduleDeps = Seq(hocon)

  override def scalacOptions =
    defaultScalaOpts ++ Seq("-Ymacro-annotations", "-Ymacro-debug-lite")

  override def ivyDeps = {
    Agg(
      libs.zio,
      libs.circeGeneric,
      libs.circeYaml
    )
  }
}

object timpigden extends MyModule {

  override def ivyDeps = {
    Agg(
      libs.zio,
      libs.zioCats,
      libs.http4sBlazeClient,
      libs.http4sBlazeServer,
      libs.http4sDsl,
      libs.scalaXml,
      libs.cats
    )
  }
}
