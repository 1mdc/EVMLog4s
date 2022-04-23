## EVMLog4s

EVMLog4s is a utility to map EVM event log into case class.

This library is **NOT compatible with scala 2.x** as it uses [type class derivation](https://docs.scala-lang.org/scala3/reference/contextual/derivation.html) which is a new feature in scala 3

### Install

Add repository to `build.sbt`
```
resolvers += Resolver.githubPackages("1mdc")
```

Add library to `build.sbt`
```
libraryDependencies += "com.hoangong" % "evmlog4s" % "1.0.0"
```

### Usage

```
import scala.deriving.*

// declare case class that derives from EthLogLine
case class Transfer(from: SAddress, to: SAddress, amount: BigInt) derives EthLogLine

// declare connection to ETH client
given web3jWrapper: Web3jWrapper = Web3jWrapper("https://mainnet.infura.io/v3/...")

EthLogParser().parserLogsFromBlocks(
  BigInt(14469640), // from block number
  BigInt(14469645), // to block number
  List(
    "0xdac17f958d2ee523a2206206994597c13d831ec7", // list of smart contracat address that you want to listen to
    "0x7f268357a8c2552623316e2562d90e642bb538e5"
  ),
  Map(
    "Transfer(address,address,uint256)" -> summon[EthLogLine[Transfer]] // map of signature of the event to EthLogLine typed class
  )
) foreach {
  case t: ParsedEvent[Transfer]       => println(s"found transfer ${t}") // This is up to you how you want to handle the event here
  case _                            => println("unknown")
}
```

### Data types mapping

| Solidity | Scala       |
|----------|-------------|
| string   | String      |
| address  | SAddress    |
| uint256  | BigInt      |
| byte32   | Array[Byte] |