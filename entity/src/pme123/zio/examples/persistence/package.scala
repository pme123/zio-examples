package pme123.zio.examples

import zio.{RIO, ZIO}

package object persistence{

  case class User(id: Int, name: String)

  case class UserNotFound(id: Int) extends Throwable
}
