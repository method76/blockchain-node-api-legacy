package com.method76.blockchain.node.gsonObjects;

import com.method76.blockchain.node.gsonObjects.bitcoin.RpcError;
import lombok.Data;

@Data public class BitcoinStringResponse {
  
    private int id;
    private String result;
    private RpcError error;

}
