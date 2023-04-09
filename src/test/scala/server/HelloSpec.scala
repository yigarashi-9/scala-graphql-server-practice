package server

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class HelloSpec extends AnyFunSpec with Matchers {
  describe("Main") {
    it("should say hello") {
      Hello.greeting shouldBe "hello"
    }
  }
}
