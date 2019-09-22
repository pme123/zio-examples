package pme123.zio.examples.api

import cats.effect.ExitCode
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import pme123.zio.examples.configuration.Configuration
import pme123.zio.examples.persistence.Persistence
import pme123.zio.examples.{configuration, persistence}
import zio.blocking.Blocking
import zio.clock.Clock
import zio.interop.catz._
import zio.{Task, ZIO, _}

object ApiApp extends App {

  type AppEnvironment = Clock with Persistence

  type AppTask[A] = RIO[AppEnvironment, A]

  def run(args: List[String]): ZIO[Environment, Nothing, Int] =
    program.fold(_ => 1, _ => 0)

  private lazy val program = for {
    conf <- configuration.load.provide(Configuration.Live)
    blockingEC <- blocking.blockingExecutor.map(_.asEC).provide(Blocking.Live)

    transactorR = Persistence.mkTransactor(
      conf.dbConfig,
      Platform.executor.asEC,
      blockingEC
    )

    httpApp = Router[AppTask](
      "/users" -> Api(s"${conf.api.endpoint}/users").routes
    ).orNotFound

    server = ZIO.runtime[AppEnvironment].flatMap { implicit rts =>
      persistence.createTable *>
        BlazeServerBuilder[AppTask]
          .bindHttp(conf.api.port, "0.0.0.0")
          .withHttpApp(CORS(httpApp))
          .serve
          .compile[AppTask, AppTask, ExitCode]
          .drain
    }

    program <- transactorR.use { transactor =>
      server.provideSome[Environment] { _ =>
        new Clock.Live with Persistence.Live {
          override protected def tnx: doobie.Transactor[Task] = transactor
        }
      }
    }
  } yield program
}

