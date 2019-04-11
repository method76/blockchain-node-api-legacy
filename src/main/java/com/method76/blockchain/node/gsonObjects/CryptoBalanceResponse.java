package com.method76.blockchain.node.gsonObjects;

import com.method76.blockchain.node.gsonObjects.abstracts.BitcoinRpcResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false) @Data 
public class CryptoBalanceResponse extends BitcoinRpcResponse {
	private double result;
	public CryptoBalanceResponse() {}
}
