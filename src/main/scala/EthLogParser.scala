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

def processRawLog[T <: EthLogLine[_]](
    log: Log,
    signatures: Map[String, T]
): Option[_] = {
  log.getTopics.asScala
    .take(1)
    .headOption
    .flatMap(sign =>
      signatures
        .find(ii =>
          Hash
            .sha3(org.web3j.utils.Numeric.toHexString(ii._1.getBytes))
            .toLowerCase == sign.toLowerCase
        )
        .map(ii => {
          val params = log.getTopics.asScala
            .drop(1)
            .map(_.replace("0x", ""))
            .toList ++ log.getData
            .replace("0x", "")
            .grouped(64)
            .toList
          ii._2.parseObj(params)
        })
    )
}

final case class ParsedLog[T](
    transaction: SAddress,
    parsedRecord: T,
    data: String,
    topics: List[String],
    blockNumber: BigInt,
    blockHash: SAddress
)

class EthLogParser()(using web3: Web3jWrapper):

  def streamBlocks[T <: EthLogLine[_]](
      fromBlockNumber: BigInt,
      addresses: List[String],
      signatures: Map[String, T]
  ): Flowable[ParsedLog[_]] =
    val fromBlock = new DefaultBlockParameterNumber(fromBlockNumber.bigInteger)
    web3
      .ethLogFlowable(
        new EthFilter(
          fromBlock,
          DefaultBlockParameterName.LATEST,
          addresses.asJava
        )
      )
      .flatMap(log => {
        val t = processRawLog(log, signatures)
        if t.isDefined then
          Flowable.just(
            ParsedLog(
              SAddress(log.getTransactionHash),
              t.get,
              log.getData,
              log.getTopics.asScala.toList,
              BigInt(log.getBlockNumber),
              SAddress(log.getBlockHash)
            )
          )
        else Flowable.empty()
      })

  def parserLogsFromBlocks[T <: EthLogLine[_]](
      fromBlockNumber: BigInt,
      toBlockNumber: BigInt,
      addresses: List[String],
      signatures: Map[String, T]
  ): List[ParsedLog[_]] =
    val fromBlock = new DefaultBlockParameterNumber(fromBlockNumber.bigInteger)
    val toBlock = new DefaultBlockParameterNumber(toBlockNumber.bigInteger)
    val logRequest = web3
      .ethGetLogs(new EthFilter(fromBlock, toBlock, addresses.asJava))
    println(s"found ${logRequest.getLogs.size()} logs")
    logRequest.getLogs.asScala
      .flatMap(logResult => {
        logResult.get() match {
          case log: Log =>
            processRawLog(log, signatures).map(parsedLine =>
              ParsedLog(
                SAddress(log.getTransactionHash),
                parsedLine,
                log.getData,
                log.getTopics.asScala.toList,
                BigInt(log.getBlockNumber),
                SAddress(log.getBlockHash)
              )
            )
          case _ =>
            println("Unknown log type")
            None
        }
      })
      .toList
