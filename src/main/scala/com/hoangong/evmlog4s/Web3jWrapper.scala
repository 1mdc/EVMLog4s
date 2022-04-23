package com.hoangong.evmlog4s

import io.reactivex.Flowable
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.core.methods.response.{EthLog, Log}
import org.web3j.protocol.http.HttpService

class Web3jWrapper(rpcUrl: String) {
  val web3 = Web3j.build(new HttpService(rpcUrl))

  def ethLogFlowable(ethFilter: EthFilter): Flowable[Log] = web3.ethLogFlowable(ethFilter)
  def ethGetLogs(ethFilter: EthFilter): EthLog = web3.ethGetLogs(ethFilter).send()
}
