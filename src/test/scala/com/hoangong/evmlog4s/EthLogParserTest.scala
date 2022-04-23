package com.hoangong.evmlog4s

import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.core.methods.response.EthLog

class EthLogParserTest extends munit.FunSuite {
  test("should parse log with only topic") {
    case class TestMessage() derives EthLogLine
    class Test extends Web3jWrapper(""):
      override def ethGetLogs(ethFilter: EthFilter): List[WrappedEthLog] =
        List(
          WrappedEthLog(
            hash = SAddress("0x11"),
            topics = List(
              "0xd854278016dc3ac42aef8d423d936f9f37eea6f9a640f8a189f44247f1282c2c"
            ),
            data = "",
            BigInt(0)
          )
        )
    given Test()
    assertEquals(
      TestMessage(),
      EthLogParser()
        .parserLogsFromBlocks(
          BigInt(0),
          BigInt(0),
          List("0x123"),
          Map("TestMessage()" -> summon[EthLogLine[TestMessage]])
        )(0)
        .parsedRecord
        .asInstanceOf[TestMessage]
    )
  }
  test("should parse log with topic and data") {
    case class TestMessage(a: String) derives EthLogLine
    class Test extends Web3jWrapper(""):
      override def ethGetLogs(ethFilter: EthFilter): List[WrappedEthLog] =
        List(
          WrappedEthLog(
            hash = SAddress("0x11"),
            topics = List(
              "0x6e7f6f90a1cd2024298ea1288ad084a044ff77bc1efe24c28907b7d2afebdca1"
            ),
            data =
              "0x0000000000000000000000000000000000000000000000000000000000616161",
            BigInt(0)
          )
        )
    given Test()
    assertEquals(
      TestMessage("aaa"),
      EthLogParser()
        .parserLogsFromBlocks(
          BigInt(0),
          BigInt(0),
          List("0x123"),
          Map("TestMessage(string)" -> summon[EthLogLine[TestMessage]])
        )(0)
        .parsedRecord
        .asInstanceOf[TestMessage]
    )
  }
  test("should parse log that has less fields than case class") {
    case class TestMessage() derives EthLogLine
    class Test extends Web3jWrapper(""):
      override def ethGetLogs(ethFilter: EthFilter): List[WrappedEthLog] =
        List(
          WrappedEthLog(
            hash = SAddress("0x11"),
            topics = List(
              "0x6e7f6f90a1cd2024298ea1288ad084a044ff77bc1efe24c28907b7d2afebdca1"
            ),
            data =
              "0x0000000000000000000000000000000000000000000000000000000000616161",
            BigInt(0)
          )
        )
    given Test()
    assertEquals(
      TestMessage(),
      EthLogParser()
        .parserLogsFromBlocks(
          BigInt(0),
          BigInt(0),
          List("0x123"),
          Map("TestMessage(string)" -> summon[EthLogLine[TestMessage]])
        )(0)
        .parsedRecord
        .asInstanceOf[TestMessage]
    )
  }
  test("should not parse log that has more fields than case class") {
    case class TestMessage(a: String) derives EthLogLine
    class Test extends Web3jWrapper(""):
      override def ethGetLogs(ethFilter: EthFilter): List[WrappedEthLog] =
        List(
          WrappedEthLog(
            hash = SAddress("0x11"),
            topics = List(
              "0xd854278016dc3ac42aef8d423d936f9f37eea6f9a640f8a189f44247f1282c2c"
            ),
            data = "",
            BigInt(0)
          )
        )
    given Test()
    assertEquals(
      0,
      EthLogParser()
        .parserLogsFromBlocks(
          BigInt(0),
          BigInt(0),
          List("0x123"),
          Map("TestMessage()" -> summon[EthLogLine[TestMessage]])
        )
        .length
    )
  }
  test("should ignore unmatched signature") {
    case class TestMessage() derives EthLogLine
    class Test extends Web3jWrapper(""):
      override def ethGetLogs(ethFilter: EthFilter): List[WrappedEthLog] =
        List(
          WrappedEthLog(
            hash = SAddress("0x11"),
            topics = List(
              "0xd854278016dc3ac42aef8d423d936f9f37eea6f9a640f8a189f44247f1282cxx"
            ),
            data = "",
            BigInt(0)
          )
        )
    given Test()
    assertEquals(
      0,
      EthLogParser()
        .parserLogsFromBlocks(
          BigInt(0),
          BigInt(0),
          List("0x123"),
          Map("TestMessage()" -> summon[EthLogLine[TestMessage]])
        )
        .length
    )
  }
}
