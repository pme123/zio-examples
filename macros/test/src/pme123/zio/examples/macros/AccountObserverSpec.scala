package pme123.zio.examples.macros

object AccountObserverSpec {/*
    extends DefaultRunnableSpec(
      suite("processEvent") {

        val event = "MyAccessEvent"
        val app: ZIO[AccountObserver with MockConsole, Nothing, Unit] = AccountObserver.>.processEvent(event)
        val mockEnv: MockSpec[_, _, String] = (
          MockSpec.expectIn(MockConsole.putStrLn)(
            equalTo(s"Got $event")
          ) *>
            MockSpec.expectOut(MockConsole.getStrLn)("42") *>
            MockSpec.expectIn(MockConsole.putStrLn)(
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
    )*/
}