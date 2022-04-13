import io.reactivex.Flowable
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

def processRawLog[T <: EthLog[_]](
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

final case class ParsedLog[T](parsedRecord: Option[T], data: String, topics: List[String], blockNumber: BigInt, blockHash: SAddress)

class EthLogParser(rpcUrl: String):
  lazy val web3 = Web3j.build(new HttpService(rpcUrl));

  def streamBlocks[T <: EthLog[_]](
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
      .map(log => ParsedLog(processRawLog(log, signatures), log.getData, log.getTopics.asScala.toList, BigInt(log.getBlockNumber), SAddress(log.getBlockHash)))

  def parserLogsFromBlocks[T <: EthLog[_]](
      fromBlockNumber: BigInt,
      toBlockNumber: BigInt,
      addresses: List[String],
      signatures: Map[String, T]
  ): List[ParsedLog[_]] =
    val fromBlock = new DefaultBlockParameterNumber(fromBlockNumber.bigInteger)
    val toBlock = new DefaultBlockParameterNumber(toBlockNumber.bigInteger)
    val logRequest = web3
      .ethGetLogs(new EthFilter(fromBlock, toBlock, addresses.asJava))
      .send()
    println(s"found ${logRequest.getLogs.size()} logs")
    logRequest.getLogs.asScala
      .flatMap(logResult => {
        logResult.get() match {
          case log: Log => Some(ParsedLog(processRawLog(log, signatures), log.getData, log.getTopics.asScala.toList, BigInt(log.getBlockNumber), SAddress(log.getBlockHash)))
          case _ =>
            println("Unknown log type")
            None
        }
      })
      .toList
