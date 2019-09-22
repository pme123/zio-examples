package pme123.zio.examples

import zio.{RIO, ZIO}

package object persistence extends Persistence.Service[Persistence]{

  final val persistenceService: ZIO[Persistence, Nothing, Persistence.Service[Any]] =
    ZIO.access(_.persistence)

  final val createTable: RIO[Persistence, Unit] = ZIO.accessM(_.persistence.createTable)

  final def all(): RIO[Persistence, Seq[User]] = ZIO.accessM(_.persistence.all())
  final def get(id: Int): RIO[Persistence, User] = ZIO.accessM(_.persistence.get(id))

  final def create(user: User): RIO[Persistence, User] = ZIO.accessM(_.persistence.create(user))

  final def delete(id: Int): RIO[Persistence, Unit] = ZIO.accessM(_.persistence.delete(id))


  case class User(id: Int, name: String)

  case class UserNotFound(id: Int) extends Throwable
}
