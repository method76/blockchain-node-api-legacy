package com.method76.blockchain.node.gsonObjects.bitcoin;

import lombok.Data;

@Data public class BitcoinListAddressBalanceResponse {
  
    private int id;
    private RpcError error;
    private AddressBalance[] result;
    
    @Data public class AddressBalance {
    	private String address;
    	private String account;
    	private double amount;
    	private String label;
    	private String[] txtds;
    }

}
