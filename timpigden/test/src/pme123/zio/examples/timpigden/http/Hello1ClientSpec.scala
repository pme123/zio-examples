package pme123.zio.examples.timpigden.http

import zio.test._
import zio.test.Assertion.{equalTo, _}
import org.http4s._
import org.http4s.client.Client
import org.http4s.implicits._
import zio.{Task, ZIO}

object Hello1ClientSpec extends DefaultRunnableSpec(

  suite("Hello1ClientSpec routes suite")(
    testM("test get") {
      for{
        client <- ZIO.access[Client[Task]](x => x)
         req = Request[Task](Method.GET, uri"http://localhost:8088/")
        asserted <- assertM(client.status(req), equalTo(Status.Ok))
      }yield asserted
    }.provideManagedShared(TestClient.clientManaged)
  )
)
