import mill._
import mill.scalalib._

trait MyModule extends ScalaModule {
  def scalaVersion = "2.13.1"

  object version {
    val cats = "2.0.0"
    val circe = "0.12.1"
    val doobie = "0.8.0-RC1"
    val http4s = "0.21.0-M4"
    val pureconfig = "0.11.1"
    val sttp = "1.6.3"
    val zio = "1.0.0-RC16"
    val zioMacros = "0.4.0"
    val zioCats = "2.0.0.0-RC6"
    val scalaTest = "3.0.8"
  }

  object libs {
    val cats = ivy"org.typelevel::cats-core:${version.cats}"
    val circeCore = ivy"io.circe::circe-core:${version.circe}"
    val circeGeneric = ivy"io.circe::circe-generic:${version.circe}"
    val doobieCore = ivy"org.tpolecat::doobie-core:${version.doobie}"
    val doobieH2 = ivy"org.tpolecat::doobie-h2:${version.doobie}"
    val http4sBlazeServer =
      ivy"org.http4s::http4s-blaze-server:${version.http4s}"
    val http4sCirce = ivy"org.http4s::http4s-circe:${version.http4s}"
    val http4sCore = ivy"org.http4s::http4s-core:${version.http4s}"
    val http4sDsl = ivy"org.http4s::http4s-dsl:${version.http4s}"
    val http4sServer = ivy"org.http4s::http4s-server:${version.http4s}"
    val pureconfig =
      ivy"com.github.pureconfig::pureconfig:${version.pureconfig}"
    val sttpCore = ivy"com.softwaremill.sttp::core:${version.sttp}"
    val sttpClient =
      ivy"com.softwaremill.sttp::async-http-client-backend-zio:${version.sttp}"
    val sttpCirce = ivy"com.softwaremill.sttp::circe::${version.sttp}"
    val zio = ivy"dev.zio::zio:${version.zio}"
    val zioMacrosAccess = ivy"dev.zio::zio-macros-access:${version.zioMacros}"
    val zioMacrosMockable = ivy"dev.zio::zio-macros-mock:${version.zioMacros}"
    val zioStream = ivy"dev.zio::zio-streams:${version.zio}"
    val zioCats = ivy"dev.zio::zio-interop-cats:${version.zioCats}"
  }

  object test extends Tests {
    override def ivyDeps = Agg(
      ivy"org.scalatest::scalatest:${version.scalaTest}",
      ivy"dev.zio::zio-test:${version.zio}",
      ivy"dev.zio::zio-test-sbt:${version.zio}"
    )

    def testOne(args: String*) = T.command {
      super.runMain("org.scalatest.run", args: _*)
    }

    def testFrameworks =
      Seq("org.scalatest.tools.Framework", "zio.test.sbt.ZTestFramework")
  }

}

object entity extends MyModule {
  override def ivyDeps = {
    Agg(
      libs.cats,
      libs.circeCore,
      libs.circeGeneric,
      libs.doobieCore,
      libs.doobieH2,
      libs.http4sBlazeServer,
      libs.http4sCirce,
      libs.http4sCore,
      libs.http4sDsl,
      libs.http4sServer,
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

  override def scalacOptions = Seq("-Ymacro-annotations", "-Ymacro-debug-lite", "-Ypartial-unification")

  override def ivyDeps = {
    Agg(
      libs.zio,
      libs.zioMacrosAccess,
      libs.zioMacrosMockable
    )
  }
}
