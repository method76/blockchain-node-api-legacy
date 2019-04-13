package com.method76.blockchain.node.gsonObjects;

import com.method76.blockchain.node.gsonObjects.abstracts.BitcoinRpcResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DashboardResponse extends BitcoinRpcResponse {

	private Result result;
	public DashboardResponse() {
	    this.result = new Result();
    }

	public void setResult(long startCount, long syncCount, long bestCount, double balance) {
    	this.result.setStartCount(startCount);
        this.result.setSyncCount(syncCount);
        this.result.setBestCount(bestCount);
        this.result.setBalance(balance);
    }

    @EqualsAndHashCode(callSuper = false) @Data
    public class Result {
        long startCount;
        long syncCount;
        long bestCount;
        double balance;
    }

}
