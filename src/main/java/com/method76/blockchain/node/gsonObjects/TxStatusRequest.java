package com.method76.blockchain.node.gsonObjects;

import lombok.Data;

@Data public class TxStatusRequest {
  
    private String symbol;
    private String orderId;

    public TxStatusRequest() {}
    
    public TxStatusRequest(String symbol, String orderId) {
        this.symbol = symbol;
        this.orderId = orderId;
    }
    
    public TxStatusRequest(TxStatusRequest req) {
        this.symbol = req.symbol;
        this.orderId = req.orderId;
    }
    
}
