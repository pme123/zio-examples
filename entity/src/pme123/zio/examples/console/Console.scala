package pme123.zio.examples.console

import zio.{RIO, Ref, UIO, ZIO}


trait Console extends Serializable {
  val console: Console.Service[Any]
}

object Console {

  trait Service[R] {
    def println(line: String): RIO[R, Unit]

    val readLine: RIO[R, String]
  }

  object > extends Service[Console] {

    final val console: ZIO[Console, Nothing, Service[Any]] =
      ZIO.access(_.console)

    final def println(line: String): RIO[Console, Unit] =
      ZIO.accessM(_.console.println(line))

    final val readLine: RIO[Console, String] =
      ZIO.accessM(_.console.readLine)
  }

  trait Live extends Console {
    override val console: Service[Any] = new Service[Any] {
      final def println(line: String): UIO[Unit] =
        UIO(scala.Console.println(line))

      final val readLine: UIO[String] =
        UIO(scala.io.StdIn.readLine())
    }
  }

  object Live extends Live

  case class Test(ref: Ref[Vector[String]]) extends Console {
    override val console: Console.Service[Any] = new Console.Service[Any] {
      final def println(line: String): UIO[Unit] =
        ref.update(_ :+ line).unit

      final val readLine: UIO[String] =
        UIO("Pascal")
    }
  }
}