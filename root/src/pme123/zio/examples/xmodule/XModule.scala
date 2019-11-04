package pme123.zio.examples.xmodule

import pme123.zio.examples.xmodule.XModule.Service
import zio.{UIO, ZIO}

trait XModule {
  val xModule: XModule.Service[Any]
}

object XModule {
  case class X(x: String = "x", y: String = "y", z: String = "z")

  trait Service[R] {
    def x: ZIO[R, Nothing, X]
  }

  trait Live extends XModule {

    def xInstance: X

    val xModule: Service[Any] = new Service[Any] {
      def x: ZIO[Any, Nothing, X] = UIO(xInstance)
    }
  }

  object > extends Service[XModule] {
    def x: ZIO[XModule, Nothing, X] = ZIO.accessM(_.xModule.x)
  }
}
