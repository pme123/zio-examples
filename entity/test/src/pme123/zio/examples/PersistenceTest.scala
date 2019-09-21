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


  trait Test
    extends Persistence {

    def users: Ref[Vector[User]]

    override val persistence: Service[Any] = new Service[Any] {
      val createTable: Task[Unit] =
        Ref.make(Vector.empty[User]).unit

      def get(id: Int): Task[User] =
        users.get.flatMap(users => Task.require(UserNotFound(id))(
          Task.succeed(users.find(_.id == id))))

      def create(user: User): Task[User] =
        users.update(_ :+ user).map(_ => user)

      def delete(id: Int): Task[Unit] =
        users.update(users => users.filterNot(_.id == id)).unit
    }
  }

  "The persistence" should {
    "be loaded" in {
      new DefaultRuntime {}.unsafeRun(
        for {
          usersRef <- Ref.make(Vector.empty[User])
          _ <-
            createAndPersist.provideSome[Any]{_=>
              new Console.Live with Test {
                val users: Ref[Vector[User]] = usersRef
              }}
          result <- usersRef.get
        } yield result
      ) shouldBe Vector(User(2, "heidi"))
    }
  }
}
