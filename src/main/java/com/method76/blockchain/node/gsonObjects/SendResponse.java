package com.method76.blockchain.node.gsonObjects;

import com.method76.blockchain.node.gsonObjects.abstracts.BitcoinRpcResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false) @Data 
public class SendResponse extends BitcoinRpcResponse {
  
    private Result result;
    public void setResult(SendRequest req) {
    	this.result = new Result(req);
    }
    
    @EqualsAndHashCode(callSuper = false) @Data 
    public class Result extends SendRequest {
    	private char status;
    	public Result(SendRequest req) {
    		super(req);
    	}
    }
    
}
