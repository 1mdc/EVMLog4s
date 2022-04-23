package com.hoangong.evmlog4s

import com.typesafe.scalalogging.LazyLogging
import io.reactivex.{Flowable, Observable}
import org.web3j.crypto.Hash
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.core.methods.response.Log
import org.web3j.protocol.core.{
  DefaultBlockParameterName,
  DefaultBlockParameterNumber
}
import org.web3j.protocol.http.HttpService

import java.lang.reflect.Field
import scala.compiletime.*
import scala.deriving.*
import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success, Try}

def processRawLog[T <: EthLogLine[_]](
    log: WrappedEthLog,
    signatures: Map[String, T]
): Option[Try[_]] = {
  val eventName :: eventData = log.topics
  signatures
    .find { case (signStr, _) =>
      Hash
        .sha3(org.web3j.utils.Numeric.toHexString(signStr.getBytes))
        .toLowerCase == eventName.toLowerCase
    }
    .map { case (_, signType) =>
      val params = eventData
        .map(_.replace("0x", "")) ++ log.data
        .replace("0x", "")
        .grouped(64)
        .toList
      Try(signType.parseObj(params))
    }
}

final case class ParsedEvent[T](
    transaction: SAddress,
    parsedRecord: T,
    data: String,
    topics: List[String],
    blockNumber: BigInt
)

class EthLogParser()(using web3: Web3jWrapper) extends LazyLogging:

  def parserLogsFromBlocks[T <: EthLogLine[_]](
      fromBlockNumber: BigInt,
      toBlockNumber: BigInt,
      addresses: List[String],
      signatures: Map[String, T]
  ): List[ParsedEvent[_]] =
    val fromBlock = new DefaultBlockParameterNumber(fromBlockNumber.bigInteger)
    val toBlock = new DefaultBlockParameterNumber(toBlockNumber.bigInteger)
    val logs = web3
      .ethGetLogs(new EthFilter(fromBlock, toBlock, addresses.asJava))
    logger.debug(s"found ${logs.length} logs")
    logs
      .flatMap(log =>
        processRawLog(log, signatures).flatMap(parsedLog =>
          parsedLog match {
            case Success(value) =>
              Some(
                ParsedEvent(
                  log.hash,
                  value,
                  log.data,
                  log.topics,
                  log.blockNumber
                )
              )
            case Failure(exception) =>
              logger.warn("unable to parse line due to error", exception);
              None
          }
        )
      )
