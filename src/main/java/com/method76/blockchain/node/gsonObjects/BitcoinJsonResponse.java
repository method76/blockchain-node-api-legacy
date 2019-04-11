package com.method76.blockchain.node.gsonObjects;

import com.method76.blockchain.node.gsonObjects.bitcoin.RpcError;
import lombok.Data;
import org.json.JSONObject;

@Data public class BitcoinJsonResponse {
  
    private int id;
    private RpcError error;
    private JSONObject result;

}
