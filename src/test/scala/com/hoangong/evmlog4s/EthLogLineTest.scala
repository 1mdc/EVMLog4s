package com.hoangong.evmlog4s

import junit.framework.TestCase
import scala.deriving.*

class EthLogLineTest extends munit.FunSuite {
  test("should parse string") {
    case class Test(a: String) derives EthLogLine
    assertEquals(Test("abc"), summon[EthLogLine[Test]].parseObj(List("616263")))
  }
  test("should parse address") {
    case class Test(a: SAddress) derives EthLogLine
    assertEquals(
      Test(SAddress("abc")),
      summon[EthLogLine[Test]].parseObj(List("abc"))
    )
  }
  test("should parse bytes") {
    case class Test(a: Array[Byte]) derives EthLogLine
    val actual = summon[EthLogLine[Test]].parseObj(
      List(org.web3j.utils.Numeric.toHexString("abc".getBytes))
    );
    val expected = Test("abc".getBytes)
    assert(actual.isInstanceOf[Test])
    assert((expected.a diff actual.a).isEmpty)
  }
  test("should parse big int") {
    case class Test(a: BigInt) derives EthLogLine
    assertEquals(
      Test(BigInt("99999999999999999999")),
      summon[EthLogLine[Test]].parseObj(
        List("3939393939393939393939393939393939393939")
      )
    )
  }
  test("should handle invalid big int") {
    case class Test(a: BigInt) derives EthLogLine
    intercept[java.lang.NumberFormatException](
      summon[EthLogLine[Test]].parseObj(List("616161"))
    )
  }
  test("should throw error is less fields than case class") {
    case class Test(a: String, b: String) derives EthLogLine
    intercept[java.lang.IndexOutOfBoundsException](
      summon[EthLogLine[Test]].parseObj(List("616263"))
    )
  }
  test("should handle unmatched more fields than case class") {
    case class Test(a: String) derives EthLogLine
    assertEquals(
      Test("abc"),
      summon[EthLogLine[Test]].parseObj(List("616263", "616263"))
    )
  }
  test("should compare address case insensitive") {
    assert(SAddress("0xAbc123") == SAddress("0xABC123"))
  }
}
