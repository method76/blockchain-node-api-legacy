package com.method76.blockchain.node.gsonObjects.ethereum;

import com.method76.blockchain.node.gsonObjects.bitcoin.RpcError;
import lombok.Data;

@Data public class EthBlock {
  
    private int id;
    private RpcError error;
    private Result result;
    
    @Data
    public class Result {
        private String gasLimit;
        private String gasUsed;
        private String hash;
        private String number;
        private String timestamp;
        private EthTransaction[] transactions;
    }
    
}
