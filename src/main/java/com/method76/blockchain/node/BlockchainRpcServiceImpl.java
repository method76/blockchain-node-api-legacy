package com.method76.blockchain.node;

import com.method76.blockchain.node.services.abstracts.BitcoinAbstractService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


/**
 * BTC Services
 * @author sungjoon.kim
 */
@Service("blockchainRpcService")
class BlockchainRpcServiceImpl extends BitcoinAbstractService {

	@Getter private String symbol = SYMBOL_BTC;
    @Getter @Value("${app.crypto.rpcurl}") private String rpcurl;
    @Getter @Value("${app.crypto.rpcid}") private String rpcid;
    @Getter @Value("${app.crypto.rpcpw}") private String rpcpw;
	@Getter @Value("${app.crypto.decimals}") private int decimals;
    @Getter @Value("${app.crypto.masteraccount}") private String masteraccount;
    @Getter @Value("${app.crypto.useraccount}") private String useraccount;
    @Getter @Value("${app.crypto.sendaddr}") private String sendaddr;
    @Getter @Value("${app.crypto.pp}") private String pp;
    @Getter @Value("${app.crypto.blockStartFrom}") private long blockStartFrom;
    @Getter @Value("${app.crypto.minconfirm}") private long minconfirm;
    @Getter @Value("${app.crypto.minamtgather}") private double minamtgather;
    @Getter @Value("${app.crypto.mingasamt}") private double mingasamt; // not using
    
}
