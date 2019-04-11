package com.method76.blockchain.node.domain.primarykeys;

import lombok.Data;

import java.io.Serializable;

@Data public class PkSymbolUid implements Serializable {
  
	private static final long serialVersionUID = 4311946119884324792L;
	private String symbol;
    private int uid;
      
    public PkSymbolUid() {}
    public PkSymbolUid(int uid, String symbol) {
        super();
        this.uid = uid;
        this.symbol = symbol;
    }
  
}