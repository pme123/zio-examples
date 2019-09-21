package pme123.zio.examples.persistence

import pme123.zio.examples.console._
import pme123.zio.examples.configuration
import pme123.zio.examples.configuration.Configuration
import pme123.zio.examples.persistence.Persistence._
import zio.blocking.Blocking
import zio.{App, Task, ZIO, blocking}

object UserRepoApp
  extends App
    with UserRepo {

  def run(args: List[String]): ZIO[Environment, Nothing, Int] =
    program.fold(_ => 1, _ => 0)

  private lazy val program = for {
    conf <- configuration.load.provide(Configuration.Live)
    blockingEC <-
      blocking.blockingExecutor.map(_.asEC).provide(Blocking.Live)
    transactorR = mkTransactor(
      conf.dbConfig,
      Platform.executor.asEC,
      blockingEC
    )
    server = ZIO.runtime[Persistence].flatMap { implicit rts =>
      createTable *>
        createAndPersist
    }
    program <- transactorR.use { transactor =>
      server.provideSome[Environment] { _ =>
        new Persistence.Live
          with Console.Live {
          override protected def tnx: doobie.Transactor[Task] = transactor
        }
      }
    }
  } yield program
}
