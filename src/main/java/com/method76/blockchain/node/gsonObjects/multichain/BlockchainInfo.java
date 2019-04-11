package com.method76.blockchain.node.gsonObjects.multichain;

import com.method76.blockchain.node.gsonObjects.bitcoin.RpcError;
import lombok.Data;

@Data public class BlockchainInfo {
  
    private int id;
    private Result result;
    private RpcError error;

    @Data public class Result {
        private long blocks;
        private long headers;
        private String bestblockhash;
    }
    
}
