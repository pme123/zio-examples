package pme123.zio.examples.persistence
import pme123.zio.examples.console._
import zio.RIO


trait UserRepo {
  val createAndPersist: RIO[Persistence with Console, Seq[User]] =
    for {
      _ <- createTable
      u1 <- create(User(1, "peter"))
      _ <- println(s"User1 persisted: $u1")
      u2 <- create(User(2, "heidi"))
      _ <- println(s"User2 persisted: $u2")
      _ <- get(u1.id)
      _ <- get(u2.id)
      _ <- delete(u1.id)
      users <- all()
      _ <- println(s"Result Users: $users")
    } yield users
}
