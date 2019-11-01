package pme123.zio.examples.macros

import zio.ZIO
import zio.console.Console
import zio.test.Assertion._
import zio.test._
import zio.test.mock.{MockConsole, MockSpec}

object AccountObserverSpec
    extends DefaultRunnableSpec(
      suite("processEvent") {
        val event = "MyAccessEvent"
        val app: ZIO[AccountObserver with MockConsole, Nothing, Unit] = AccountObserver.>.processEvent(event)
        val mockEnv: MockSpec[_, _, String] = (
          MockSpec.expectIn(MockConsole.Service.putStrLn)(
            equalTo(s"Got $event")
          ) *>
            MockSpec.expectOut(MockConsole.Service.getStrLn)("42") *>
            MockSpec.expectIn(MockConsole.Service.putStrLn)(
              equalTo("You entered: 42")
            )
        )
        testM("calls putStrLn > getStrLn > putStrLn and returns unit") {
          val result = app.provideManaged(mockEnv.map { mockConsole =>
            new AccountObserverLive with Console {
              val console = mockConsole.console
            }
          })
          assertM(result, isUnit)
        }
      }
    )
