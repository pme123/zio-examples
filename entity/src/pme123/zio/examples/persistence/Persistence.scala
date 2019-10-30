package pme123.zio.examples.persistence

import cats.effect.Blocker
import doobie.h2.H2Transactor
import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.transactor.Transactor
import doobie.util.update.Update0
import pme123.zio.examples.configuration.DbConfig
import zio._
import zio.interop.catz._

import scala.concurrent.ExecutionContext

trait Persistence extends Serializable {
  val persistence: Persistence.Service[Any]
}

object Persistence {

  trait Service[R] {
    val createTable: RIO[R, Unit]

    def all(): RIO[R, Seq[User]]

    def get(id: Int): RIO[R, User]

    def create(user: User): RIO[R, User]

    def delete(id: Int): RIO[R, Unit]
  }

  object > extends Persistence.Service[Persistence] {

    final val persistenceService
        : ZIO[Persistence, Nothing, Persistence.Service[Any]] =
      ZIO.access(_.persistence)

    final val createTable: RIO[Persistence, Unit] =
      ZIO.accessM(_.persistence.createTable)

    final def all(): RIO[Persistence, Seq[User]] =
      ZIO.accessM(_.persistence.all())

    final def get(id: Int): RIO[Persistence, User] =
      ZIO.accessM(_.persistence.get(id))

    final def create(user: User): RIO[Persistence, User] =
      ZIO.accessM(_.persistence.create(user))

    final def delete(id: Int): RIO[Persistence, Unit] =
      ZIO.accessM(_.persistence.delete(id))
  }

  trait Live extends Persistence {

    protected def tnx: Transactor[Task]

    val persistence: Service[Any] = new Service[Any] {

      final val createTable: Task[Unit] =
        SQL.createTable.run
          .transact(tnx)
          .foldM(err => Task.fail(err), _ => Task.succeed(()))

      final def get(id: Int): Task[User] =
        SQL
          .get(id)
          .option
          .transact(tnx)
          .foldM(
            Task.fail,
            maybeUser => Task.require(UserNotFound(id))(Task.succeed(maybeUser))
          )

      final def create(user: User): Task[User] =
        SQL
          .create(user)
          .run
          .transact(tnx)
          .foldM(err => Task.fail(err), _ => Task.succeed(user))

      final def delete(id: Int): Task[Unit] =
        SQL
          .delete(id)
          .run
          .transact(tnx)
          .unit
          .orDie

      final def all(): Task[Seq[User]] =
        SQL
          .all()
          .nel
          .transact(tnx)
          .foldM(err => Task.fail(err), users => Task.succeed(users.toList))

    }

    object SQL {

      def createTable: Update0 =
        sql"""CREATE TABLE IF NOT EXISTS
       USERS (id int PRIMARY KEY, name varchar)""".update

      def get(id: Int): Query0[User] =
        sql"""SELECT * FROM USERS WHERE ID = $id """.query[User]

      def create(user: User): Update0 =
        sql"""INSERT INTO USERS (ID, NAME) VALUES (${user.id},
        ${user.name})""".update

      def delete(id: Int): Update0 =
        sql"""DELETE FROM USERS WHERE ID = $id""".update

      def all(): Query0[User] =
        sql"""SELECT * FROM USERS""".query[User]
    }

  }

  def mkTransactor(
      conf: DbConfig,
      connectEC: ExecutionContext,
      transactEC: ExecutionContext
  ): Managed[Throwable, H2Transactor[Task]] = {
    import zio.interop.catz._

    val xa = H2Transactor.newH2Transactor[Task](
      conf.url,
      conf.user,
      conf.password,
      connectEC,
      Blocker.liftExecutionContext(transactEC)
    )

    val res = xa.allocated.map {
      case (transactor, cleanupM) =>
        Reservation(ZIO.succeed(transactor), _ => cleanupM.orDie)
    }.uninterruptible

    Managed(res)
  }

  trait Test extends Persistence {

    def users: Ref[Vector[User]]

    override val persistence: Service[Any] = new Service[Any] {
      val createTable: Task[Unit] =
        Ref.make(Vector.empty[User]).unit

      def get(id: Int): Task[User] =
        users.get.flatMap(
          users =>
            Task.require(UserNotFound(id))(Task.succeed(users.find(_.id == id)))
        )

      def create(user: User): Task[User] =
        users.update(_ :+ user).map(_ => user)

      def delete(id: Int): Task[Unit] =
        users.update(users => users.filterNot(_.id == id)).unit

      def all(): RIO[Any, Seq[User]] = users.get
    }
  }
}
