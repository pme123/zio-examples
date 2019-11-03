package pme123.zio.examples.persistence
import pme123.zio.examples.console._
import zio.RIO


trait UserRepo {

  val createAndPersist: RIO[Persistence with Console, Seq[User]] =
    for {
      _ <- Persistence.>.createTable
      u1 <- Persistence.>.create(User(1, "peter"))
      _ <- Console.>.println(s"User1 persisted: $u1")
      u2 <- Persistence.>.create(User(2, "heidi"))
      _ <- Console.>.println(s"User2 persisted: $u2")
      _ <- Persistence.>.get(u1.id)
      _ <- Persistence.>.get(u2.id)
      _ <- Persistence.>.delete(u1.id)
      users <- Persistence.>.all()
      _ <- Console.>.println(s"Result Users: $users")
    } yield users
}
