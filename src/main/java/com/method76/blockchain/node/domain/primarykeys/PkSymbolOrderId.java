package com.method76.blockchain.node.domain.primarykeys;

import lombok.Data;

import java.io.Serializable;

@Data public class PkSymbolOrderId implements Serializable {
  
    private static final long serialVersionUID = 8735431110586516770L;
    private String symbol;
    private String orderId;
      
    public PkSymbolOrderId() {}
    public PkSymbolOrderId(String symbol, String orderId) {
        super();
        this.symbol = symbol;
        this.orderId = orderId;
    }
  
}