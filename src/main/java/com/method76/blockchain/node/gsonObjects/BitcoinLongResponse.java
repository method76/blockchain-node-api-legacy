package com.method76.blockchain.node.gsonObjects;

import com.method76.blockchain.node.gsonObjects.abstracts.BitcoinRpcResponse;
import lombok.Data;

@Data public class BitcoinLongResponse extends BitcoinRpcResponse {
    private double result;
}
