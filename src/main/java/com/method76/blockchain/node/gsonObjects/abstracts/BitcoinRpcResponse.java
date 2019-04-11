package com.method76.blockchain.node.gsonObjects.abstracts;

import com.method76.blockchain.node.constants.abstracts.BlockchainConstant;
import lombok.Data;

@Data public class BitcoinRpcResponse implements BlockchainConstant {
    private int code = CODE_SUCCESS;
    private String error;
}
