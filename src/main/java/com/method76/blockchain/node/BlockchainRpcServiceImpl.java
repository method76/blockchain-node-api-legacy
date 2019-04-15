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

	@Getter @Value("app.crypto.type") private String symbol;
    @Getter @Value("${app.crypto.rpcurl}") private String rpcurl;
    @Getter @Value("${app.crypto.rpcid}") private String rpcid;
    @Getter @Value("${app.crypto.rpcpw}") private String rpcpw;
	@Getter @Value("${app.crypto.decimals}") private int decimals;
    @Getter @Value("${app.crypto.useraccount}") private String useraccount;
    @Getter @Value("${app.crypto.pp}") private String passphrase;
    @Getter @Value("${app.crypto.blockstartfrom}") private long blockstartfrom;
    @Getter @Value("${app.crypto.minconfirm}") private long minconfirm;

    @Getter @Value("${app.crypto.owneraccount}") private String owneraccount;
    @Getter @Value("${app.crypto.owneraddress}") private String owneraddress;

    @Getter @Value("${app.crypto.mingasamt}") private double mingasamt; // not using
    @Getter @Value("${app.crypto.minamtgather}") private double minamtgather;

}
