package com.method76.blockchain.node.domain;


import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


@Entity
@Data public class TbBlockchainMaster implements Serializable {

	private static final long serialVersionUID = 3428902633510996300L;
	
	@Column(columnDefinition="TIMESTAMP", insertable=false, updatable=true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updDt;
    @Column(columnDefinition="TIMESTAMP", insertable=false, updatable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date regDt;
    @Id
    private String symbol;
    private long bestHeight;
    private long syncHeight;
    private int decimals;
    private String sendMastAddr;
    private double lastTxFee;
    private double lastGasPrice;
    private double lastGasUsed;

    public TbBlockchainMaster() {}
    public TbBlockchainMaster(String symbol, String sendMastAddr) {
	    this.symbol = symbol;
	    this.sendMastAddr = sendMastAddr;
	}
	public TbBlockchainMaster(String symbol, String sendMastAddr, long syncHeight
			, long latestHeight) {
	    this.symbol = symbol;
	    this.sendMastAddr = sendMastAddr;
	    this.syncHeight = syncHeight;
	    this.bestHeight = latestHeight;
	}
	
	@Override public String toString() {
		return symbol + " " + syncHeight + " " + bestHeight + " " + updDt;
	}

}
