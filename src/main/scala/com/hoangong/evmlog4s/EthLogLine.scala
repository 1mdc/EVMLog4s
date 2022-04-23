package com.hoangong.evmlog4s

import scala.compiletime.*
import scala.deriving.*

trait EthLogLine[T]:
  def parseObj(hexValues: List[String]): T

inline def summonAll[T <: Tuple]: List[EthLogLine[_]] =
  inline erasedValue[T] match
    case _: EmptyTuple => Nil
    case _: (t *: ts)  => summonInline[EthLogLine[t]] :: summonAll[ts]

def toTuple(xs: List[_], acc: Tuple): Tuple =
  xs match
    case Nil      => acc
    case (h :: t) => h *: toTuple(t, acc)

object EthLogLine:
  given EthLogLine[Array[Byte]] with
    def parseObj(hexValues: List[String]): Array[Byte] =
      org.web3j.utils.Numeric.hexStringToByteArray(hexValues.head)

  given EthLogLine[String] with
    def parseObj(hexValues: List[String]): String = new String(
      org.web3j.utils.Numeric.hexStringToByteArray(hexValues.head)
    ).trim

  given EthLogLine[SAddress] with
    def parseObj(hexValues: List[String]): SAddress =
      hexValues.map(SAddress.apply).head

  given EthLogLine[BigInt] with
    def parseObj(hexValues: List[String]): BigInt =
      BigInt(
        new String(org.web3j.utils.Numeric.hexStringToByteArray(hexValues.head))
      )

  def eqProduct[T](
      p: Mirror.ProductOf[T],
      elems: => List[EthLogLine[_]]
  ): EthLogLine[T] =
    (items: List[String]) =>
      p.fromProduct(
        toTuple(
          elems.zip(items).map { case (tpy, value) =>
            tpy.parseObj(List(value))
          },
          EmptyTuple
        )
      )

  inline given derived[T](using m: Mirror.Of[T]): EthLogLine[T] =
    lazy val elemInstances = summonAll[m.MirroredElemTypes]
    inline m match
      case p: Mirror.ProductOf[T] => eqProduct(p, elemInstances)
      case _ => throw new RuntimeException("not supported type")

case class SAddress(value: String):
  def ==(address: SAddress): Boolean =
    address.value.toLowerCase == address.value.toLowerCase
