package pme123.zio.examples

import zio.test.Assertion._
import zio.test._

object ZioAssertUnitSuites
    extends DefaultRunnableSpec(suite("ZioAssertUnitSuites") {
      suite("Assertions"){
        test("Check assertions") {
          assert(Right(Some(2)), isRight(isSome(equalTo(2))))
        }
      }
      suite("case classes") {
        final case class Address(country: String, city: String)
        final case class User(name: String, age: Int, address: Address)
        test("check fields") {
          assert(
            User("Jonny", 26, Address("Denmark", "Copenhagen")),
            hasField("age", (u: User) => u.age, isGreaterThanEqualTo(18)) &&
              hasField(
                "country",
                (u: User) => u.address.country,
                not(equalTo("USA"))
              )
          )
        }
      }
    })
