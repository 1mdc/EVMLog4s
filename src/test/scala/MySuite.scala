import scala.deriving.*

class MySuite extends munit.FunSuite {
  test("example test that succeeds") {
    final case class Transfer(from: SAddress, to: SAddress, amount: BigInt)
      derives EthLogLine
    final case class OrderCancelled(hash: Array[Byte]) derives EthLogLine

    given web3jWrapper: Web3jWrapper = Web3jWrapper(
      "https://mainnet.infura.io/v3/79cb23fba7ea4af5bbb2756aad7d495b"
    )
    EthLogParser().parserLogsFromBlocks(
      BigInt(14469640),
      BigInt(14469645),
      List(
        "0xdac17f958d2ee523a2206206994597c13d831ec7",
        "0x7f268357a8c2552623316e2562d90e642bb538e5"
      ),
      Map(
        "OrderCancelled(bytes32)" -> summon[EthLogLine[OrderCancelled]],
        "Transfer(address,address,uint256)" -> summon[EthLogLine[Transfer]]
      )
    ) foreach {
      case t: ParsedLog[OrderCancelled] => println(s"found order ${t}")
      case t: ParsedLog[Transfer]       => println(s"found transfer ${t}")
      case _                            => println("unknown")
    }
  }
}
