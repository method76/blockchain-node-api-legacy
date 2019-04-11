package com.method76.blockchain.node.gsonObjects.ethereum;

import com.method76.blockchain.node.gsonObjects.bitcoin.RpcError;
import lombok.Data;

@Data public class EthListAccount implements Cloneable {
  
    private int id;
    private RpcError error;
    private String[] result;
    
}
