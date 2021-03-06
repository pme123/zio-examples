package pme123.zio.examples.macros

import zio.ZIO
import zio.macros.annotation.accessible

@accessible
trait AccessExample {
  val accessExample: AccessExample.Service[Any]
}

object AccessExample {
  trait Service[R] {
    def foo(): ZIO[R, Nothing, Unit]
    def bar(v1: Int, v2: Int): ZIO[R, Nothing, Int]
    def baz(v1: Int)(v2: Int): ZIO[R, Nothing, String]
  }

  trait Live extends AccessExample {
    final val accessExample: Service[Any] = new Service[Any] {

      import zio._

      def foo(): ZIO[Any, Nothing, Unit] =
        ZIO.effectTotal(println("foo"))

      def bar(v1: Int, v2: Int): ZIO[Any, Nothing, Int] =
        for {
          _ <- ZIO.effectTotal(println(s"bar(v1: $v1, v2: $v2)"))
          result = v1 + v2
        } yield result

      def baz(v1: Int)(v2: Int): ZIO[Any, Nothing, String] =
        for {
          _ <- ZIO.effectTotal(println(s"baz(v1: $v1)(v2: $v2)"))
          result = v1 + v2
        } yield s"The result is $result"
    }
  }
  object Live extends Live
}
