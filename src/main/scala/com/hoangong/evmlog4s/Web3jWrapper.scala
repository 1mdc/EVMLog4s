package com.hoangong.evmlog4s

import io.reactivex.Flowable
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.core.methods.response.{EthLog, Log}
import org.web3j.protocol.http.HttpService
import scala.jdk.CollectionConverters.*

class Web3jWrapper(rpcUrl: String) {
  val web3 = Web3j.build(new HttpService(rpcUrl))

  def ethLogFlowable(ethFilter: EthFilter): Flowable[Log] =
    web3.ethLogFlowable(ethFilter)
  def ethGetLogs(ethFilter: EthFilter): List[WrappedEthLog] = web3
    .ethGetLogs(ethFilter)
    .send()
    .getLogs
    .asScala
    .toList
    .flatMap(logResult => {
      logResult.get() match {
        case log: Log =>
          Option(
            WrappedEthLog(
              hash = SAddress(log.getTransactionHash),
              topics = log.getTopics.asScala.toList,
              data = log.getData,
              blockNumber = BigInt(log.getBlockNumber)
            )
          )
        case _ => None
      }
    })
}

case class WrappedEthLog(
    hash: SAddress,
    topics: List[String],
    data: String,
    blockNumber: BigInt
)
