package com.method76.blockchain.node.domain.primarykeys;

import lombok.Data;

import java.io.Serializable;

@Data public class PkSymbolTxidToAddr implements Serializable {
  
    private static final long serialVersionUID = 8735401120586516770L;
    private String symbol;
    private String txid;
    private String toAddr;
      
    public PkSymbolTxidToAddr() {}
    public PkSymbolTxidToAddr(String symbol, String txid, String toAddr) {
        super();
        this.symbol = symbol;
        this.txid = txid;
        this.toAddr = toAddr;
    }
  
}