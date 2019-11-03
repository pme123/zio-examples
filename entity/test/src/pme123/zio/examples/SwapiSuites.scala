package pme123.zio.examples

import pme123.zio.examples.configuration.{Configuration, SwapiConfig}
import pme123.zio.examples.swapi.Swapi.Test
import pme123.zio.examples.swapi.{People, ServiceException, Swapi}
import zio.test.Assertion._
import zio.test._
import zio.{Ref, ZIO}

object SwapiTests {

  //noinspection TypeAnnotation
  lazy val testSuites = suite("SwapiTestSuites") {

    suite("Get a Person for an ID")(
      testM("get Luke Skywalker") {
        for {
          peopleRef <- Ref.make(Vector(People()))
          luke <- Swapi.>.people(1).provide(Test(peopleRef))
        } yield assert(luke, equalTo(People()))
      },
      testM("get not existing People") {
        for {
          peopleRef <- Ref.make(Vector(People()))
          failure <- Swapi.>.people(2).provide(Test(peopleRef)).run
        } yield assert(
          failure,
          fails(equalTo(ServiceException("No People with id 2")))
        )
      }
    )
  }

  private lazy val liveEnv: ZIO[Any, Throwable, Swapi.Live] = for {
    conf <- Configuration.>.load.provide(Configuration.Live)
  } yield new Swapi.Live {
    protected def swapiConfig: SwapiConfig = conf.swapi
  }
  //noinspection TypeAnnotation
  lazy val liveSuites = suite("SwapiLiveSuites") {

    suite("Get a Person for an ID")(
      testM("get Luke Skywalker") {
        for {
          env <- liveEnv
          luke <- Swapi.>.people(1).provide(env)
        } yield assert(luke, equalTo(People()))
      },
      testM("get not existing People") {
        for {
          env <- liveEnv
          failure <- Swapi.>.people(9999).provide(env).run
        } yield assert(
          failure,
          fails(equalTo(ServiceException("""{"detail":"Not found"}""")))
        )
      }
    )
  }
}

object SwapiSuites
    extends DefaultRunnableSpec(
      suite("SwapiSuites")(
        SwapiTests.testSuites,
        SwapiTests.liveSuites
      )
    )
