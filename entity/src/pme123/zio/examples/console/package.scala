package pme123.zio.examples

import zio.{RIO, ZIO}

package object console extends Console.Service[Console] {

  final val consoleService: ZIO[Console, Nothing, Console.Service[Any]] =
    ZIO.access(_.console)

  final def println(line: String): RIO[Console, Unit] =
    ZIO.accessM(_.console.println(line))

  final val readLine: RIO[Console, String] =
    ZIO.accessM(_.console.readLine)
}
