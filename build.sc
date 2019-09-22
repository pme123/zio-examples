import mill._, scalalib._

object entity extends ScalaModule {
  def scalaVersion = "2.13.0"

  object version {
    val cats = "2.0.0"
    val circe = "0.12.1"
    val doobie = "0.8.0-RC1"
    val http4s = "0.21.0-M4"
    val pureconfig = "0.11.1"
    val sttp = "1.6.3"
    val zio = "1.0.0-RC12-1"
    val zioCats = "2.0.0.0-RC3"
  }


  object test extends Tests {
    override def ivyDeps = Agg(
      ivy"org.scalatest::scalatest:3.0.8"
    )

    def testOne(args: String*) = T.command {
      super.runMain("org.scalatest.run", args: _*)
    }

    def testFrameworks = Seq("org.scalatest.tools.Framework")
  }

  override def ivyDeps = {
    Agg(
      ivy"com.github.pureconfig::pureconfig:${version.pureconfig}",
      ivy"com.softwaremill.sttp::core:${version.sttp}",
      ivy"com.softwaremill.sttp::async-http-client-backend-zio:${version.sttp}",
      //    ivy"com.softwaremill.sttp::circe::${version.sttp}",
      ivy"dev.zio::zio:${version.zio}",
      ivy"dev.zio::zio-streams:${version.zio}",
      ivy"dev.zio::zio-interop-cats:${version.zioCats}",
      ivy"io.circe::circe-core:${version.circe}",
      ivy"io.circe::circe-generic:${version.circe}",
      ivy"org.http4s::http4s-blaze-server:${version.http4s}",
      ivy"org.http4s::http4s-circe:${version.http4s}",
      ivy"org.http4s::http4s-core:${version.http4s}",
      ivy"org.http4s::http4s-dsl:${version.http4s}",
      ivy"org.http4s::http4s-server:${version.http4s}",
      ivy"org.tpolecat::doobie-core:${version.doobie}",
      ivy"org.tpolecat::doobie-h2:${version.doobie}",
      ivy"org.typelevel::cats-core:${version.cats}"
    )
  }

}

