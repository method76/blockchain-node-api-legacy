package com.method76.blockchain.node.gsonObjects;

import com.method76.blockchain.node.constants.abstracts.BlockchainConstant;
import com.method76.blockchain.node.gsonObjects.abstracts.BitcoinRpcResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(callSuper = false) @Data
public class TxHistoryResponse extends BitcoinRpcResponse
		implements BlockchainConstant, Serializable {

	private static final long serialVersionUID = -7170384455007161689L;
	private Txs result;
    
    public TxHistoryResponse() {
    	this.result = new Txs();
    }

    @Data public class Txs implements Serializable {
		private static final long serialVersionUID = -3046862810265929816L;
		List<TransactionResponse> txs;
    	public Txs() {}
    }
    
}
