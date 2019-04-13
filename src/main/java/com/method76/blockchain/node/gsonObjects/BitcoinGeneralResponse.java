package com.method76.blockchain.node.gsonObjects;

import com.method76.blockchain.node.gsonObjects.bitcoin.RpcError;
import lombok.Data;

@Data public class BitcoinGeneralResponse {
    private int id;
    private RpcError error;
    private Object result;
}
