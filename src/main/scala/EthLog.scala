import scala.compiletime.*
import scala.deriving.*

trait EthLog[T]:
  def parseObj(items: List[String]): T

inline def summonAll[T <: Tuple]: List[EthLog[_]] =
  inline erasedValue[T] match
    case _: EmptyTuple => Nil
    case _: (t *: ts)  => summonInline[EthLog[t]] :: summonAll[ts]

def toTuple(xs: List[_], acc: Tuple): Tuple =
  xs match
    case Nil      => acc
    case (h :: t) => h *: toTuple(t, acc)

object EthLog:
  given EthLog[Array[Byte]] with
    def parseObj(items: List[String]): Array[Byte] =
      org.web3j.utils.Numeric.hexStringToByteArray(items.head)

  given EthLog[String] with
    def parseObj(items: List[String]): String = items.head

  given EthLog[SAddress] with
    def parseObj(items: List[String]): SAddress = items.map(SAddress.apply).head

  given EthLog[BigInt] with
    def parseObj(items: List[String]): BigInt =
      org.web3j.utils.Numeric.toBigInt(items.head)

  def eqProduct[T](
      p: Mirror.ProductOf[T],
      elems: => List[EthLog[_]]
  ): EthLog[T] =
    (items: List[String]) =>
      p.fromProduct(
        toTuple(
          elems.zip(items).map(i => i._1.parseObj(List(i._2))),
          EmptyTuple
        )
      )

  inline given derived[T](using m: Mirror.Of[T]): EthLog[T] =
    lazy val elemInstances = summonAll[m.MirroredElemTypes]
    inline m match
      case p: Mirror.ProductOf[T] => eqProduct(p, elemInstances)
      case _ => throw new RuntimeException("not supported type")

case class SAddress(value: String) {
  def ==(address: SAddress): Boolean =
    address.value.toLowerCase == address.value.toLowerCase
}
