package com.method76.blockchain.node.gsonObjects;

import com.method76.blockchain.node.gsonObjects.bitcoin.RpcError;
import lombok.Data;

@Data public class BitcoinBooleanResponse {
  
    private int id;
    private boolean result;
    private RpcError error;

}
