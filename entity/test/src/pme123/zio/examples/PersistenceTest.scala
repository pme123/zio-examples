package pme123.zio.examples

import org.scalatest.{Matchers, WordSpec}
import pme123.zio.examples.console.Console
import pme123.zio.examples.persistence.Persistence._
import pme123.zio.examples.persistence._
import zio._

class PersistenceTest
  extends WordSpec
    with Matchers
    with UserRepo {

  "The persistence" should {
    "be loaded" in {
      new DefaultRuntime {}.unsafeRun(
        for {
          usersRef <- Ref.make(Vector.empty[User])
          users <- runCreateAndPersist(usersRef)
        } yield users
      ) shouldBe Vector(User(2, "heidi"))
    }

    def runCreateAndPersist(usersRef: Ref[Vector[User]]) =
      createAndPersist.provideSome[Any] { _ =>
        new Console.Live with Test {
          val users: Ref[Vector[User]] = usersRef
        }
      }
  }
}
