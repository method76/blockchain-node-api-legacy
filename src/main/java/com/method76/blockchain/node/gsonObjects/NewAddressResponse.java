package com.method76.blockchain.node.gsonObjects;

import com.method76.blockchain.node.gsonObjects.abstracts.BitcoinRpcResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false) @Data
public class NewAddressResponse extends BitcoinRpcResponse {
  
	private Result result;
	public NewAddressResponse() {}
	public void setResult(PersonalInfoRequest req) {
    	this.result = new Result(req);
    }
    
    @EqualsAndHashCode(callSuper = false) @Data 
    public class Result extends PersonalInfoRequest {
        public Result(PersonalInfoRequest req) {
        	super(req);
        }
    }
    
}
