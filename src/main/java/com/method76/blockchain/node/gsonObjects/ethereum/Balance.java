package com.method76.blockchain.node.gsonObjects.ethereum;

import lombok.Data;

@Data public class Balance implements Cloneable {
  
    private Double amount;
    private String error;
    
}
