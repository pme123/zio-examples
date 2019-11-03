package pme123.zio.examples

import pme123.zio.examples.configuration.Configuration
import pme123.zio.examples.console.Console
import pme123.zio.examples.persistence.Persistence.mkTransactor
import pme123.zio.examples.persistence.UserRepoApp.Platform
import pme123.zio.examples.persistence._
import zio._
import zio.blocking.Blocking
import zio.test.Assertion.equalTo
import zio.test.{DefaultRunnableSpec, assert, suite, testM}

object PersistenceTests extends UserRepo {

  //noinspection TypeAnnotation
  lazy val testSuites = suite("The test persistence") {
    testM("be handled correctly") {
      for {
        usersRef <- Ref.make(Vector.empty[User])
        users <- runCreateAndPersist(usersRef)
      } yield assert(
        users,
        equalTo(Vector(User(2, "heidi")))
      )
    }
  }

  def runCreateAndPersist(usersRef: Ref[Vector[User]]) =
    createAndPersist.provideSome[Any] { _ =>
      new Console.Live with Persistence.Test {
        val users: Ref[Vector[User]] = usersRef
      }
    }

  //noinspection TypeAnnotation
  lazy val liveSuites = suite("The live persistence") {
    testM("be handled correctly") {
      for {
        usersRef <- Ref.make(Vector.empty[User])
        users <- runCreateAndPersistLive(usersRef)
      } yield assert(
        users,
        equalTo(Vector(User(2, "heidi")))
      )
    }
  }

  def runCreateAndPersistLive(usersRef: Ref[Vector[User]]) =
    for {
      conf <- Configuration.>.load.provide(Configuration.Live)
      blockingEC <- blocking.blockingExecutor.map(_.asEC).provide(Blocking.Live)
      transactorR = mkTransactor(
        conf.dbConfig,
        Platform.executor.asEC,
        blockingEC
      )
      server = ZIO.runtime[Persistence].flatMap { implicit rts =>
        Persistence.>.createTable *> createAndPersist
      }
      program <- transactorR.use { transactor =>
        server.provideSome[ZEnv] { _ =>
          new Persistence.Live with Console.Live {
            override protected def tnx: doobie.Transactor[Task] = transactor
          }
        }
      }
    } yield program
}

object PersistenceSuites
    extends DefaultRunnableSpec(
      suite("PersistenceSuites")(
        PersistenceTests.liveSuites,
        PersistenceTests.testSuites
      )
    )
