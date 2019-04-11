package com.method76.blockchain.node.gsonObjects.ethereum;

import com.method76.blockchain.node.gsonObjects.bitcoin.RpcError;
import lombok.Data;

@Data public class EthTransactionResponse {
  
    private int id;
    private EthTransaction result;
    private RpcError error;
    
    public EthTransactionResponse(RpcError error) { this.error = error; }
  
}
