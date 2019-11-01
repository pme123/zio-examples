package pme123.zio.examples.api

import cats.effect.ExitCode
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import pme123.zio.examples.configuration.Configuration
import pme123.zio.examples.console.{Console => MyConsole}
import pme123.zio.examples.persistence.Persistence
import zio.blocking.Blocking
import zio.clock.Clock
import zio.interop.catz._
import zio._
import zio.console._
object ApiApp extends App {

  type AppEnvironment = Clock with Persistence

  type AppTask[A] = RIO[AppEnvironment, A]

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    program.foldM(
      err => putStrLn(s"Program failed: $err") *> ZIO.succeed(1),
      _ => ZIO.succeed(0)
    )

  private lazy val program: ZIO[ZEnv, Throwable, Unit] = for {
    conf <- Configuration.>.load.provide(Configuration.Live)
    blockingEC <- blocking.blockingExecutor.map(_.asEC).provide(Blocking.Live)

    transactorR = Persistence.mkTransactor(
      conf.dbConfig,
      Platform.executor.asEC,
      blockingEC
    )
    httpApp = Router[AppTask](
      "/users" -> Api().routes
    ).orNotFound
    _ <- MyConsole.>.println(
      s"The server runs on http://${conf.api.endpoint}:${conf.api.port}"
    ).provide(MyConsole.Live)
    server = ZIO.runtime[AppEnvironment].flatMap { implicit rts =>
      Persistence.>.createTable *>
        BlazeServerBuilder[AppTask]
          .bindHttp(conf.api.port, conf.api.endpoint)
          .withHttpApp(CORS(httpApp))
          .serve
          .compile[AppTask, AppTask, ExitCode]
          .drain
    }

    program <- transactorR.use { transactor =>
      server.provideSome[ZEnv] { _ =>
        new Clock.Live with Persistence.Live {
          override protected def tnx: doobie.Transactor[Task] = transactor
        }
      }
    }
  } yield program
}
