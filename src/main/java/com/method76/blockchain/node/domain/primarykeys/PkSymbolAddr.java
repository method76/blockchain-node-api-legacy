package com.method76.blockchain.node.domain.primarykeys;

import lombok.Data;

import java.io.Serializable;

@Data public class PkSymbolAddr implements Serializable {
  
    private static final long serialVersionUID = 8735401110516516770L;
    private String symbol;
    private String addr;
      
    public PkSymbolAddr() {}
    public PkSymbolAddr(String symbol, String addr) {
        super();
        this.symbol = symbol;
        this.addr = addr;
    }
  
}