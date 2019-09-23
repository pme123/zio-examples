package pme123.zio.examples.console

import zio.{RIO, Ref, UIO}


trait Console extends Serializable {
  val console: Console.Service[Any]
}

object Console {

  trait Service[R] {
    def println(line: String): RIO[R, Unit]

    val readLine: RIO[R, String]
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