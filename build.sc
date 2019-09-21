import mill._, scalalib._

object entity extends ScalaModule {
  def scalaVersion = "2.13.0"

  object version {
    val cats = "2.0.0"
    val zio = "1.0.0-RC12-1"
    val zioCats = "2.0.0.0-RC3"
    val sttp = "1.6.3"
    val doobie = "0.8.0-RC1"
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
      ivy"com.github.pureconfig::pureconfig:0.11.1",
      ivy"org.typelevel::cats-core:${version.cats}",
      ivy"org.tpolecat::doobie-core:${version.doobie}",
      ivy"org.tpolecat::doobie-h2:${version.doobie}",
      ivy"dev.zio::zio:${version.zio}",
      ivy"dev.zio::zio-streams:${version.zio}",
      ivy"dev.zio::zio-interop-cats:${version.zioCats}",
      ivy"com.softwaremill.sttp::core:${version.sttp}",
      ivy"com.softwaremill.sttp::async-http-client-backend-zio:${version.sttp}",
      ivy"com.softwaremill.sttp::play-json::${version.sttp}"
    )
  }

}

