package com.method76.blockchain.node.gsonObjects;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data public class TxStatusResponse {
  
    private Result result;
    public void setResult(TxStatusRequest req) {
    	this.result = new Result(req);
    }
    
    @EqualsAndHashCode(callSuper = false) @Data 
    public class Result extends TxStatusRequest {
    	private char status;
    	private String txid;
    	public Result(TxStatusRequest req) {
    		super(req);
    	}
    }
    
}
