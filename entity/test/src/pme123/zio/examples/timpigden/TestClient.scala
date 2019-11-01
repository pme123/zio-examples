package pme123.zio.examples.timpigden

import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import zio.{Task, ZIO, ZManaged}
import zio.test.TestResult
import zio.interop.catz._

object TestClient {

  def clientManaged: ZManaged[Any, Throwable, Client[Task]] = {
    val zioManaged = ZIO.runtime[Any].map { rts =>
      val exec = rts.Platform.executor.asEC
      implicit def rr = rts
      catsIOResourceSyntax(BlazeClientBuilder[Task](exec).resource).toManaged
    }
    // for our test we need a ZManaged, but right now we've got a ZIO of a ZManaged. To deal with
    // that we create a Managed of the ZIO and then flatten it
    val mgr = zioManaged.toManaged_ // toManaged_ provides an empty release of the rescoure
    mgr.flatten
  }
}
