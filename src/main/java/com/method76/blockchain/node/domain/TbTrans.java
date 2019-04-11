package com.method76.blockchain.node.domain;

import com.method76.blockchain.node.domain.primarykeys.PkSymbolTxidToAddr;
import com.method76.blockchain.node.gsonObjects.SendRequest;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@IdClass(PkSymbolTxidToAddr.class)
public class TbTrans {

	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition="TIMESTAMP", insertable=false, updatable=false)
	private Date regDt;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition="TIMESTAMP", insertable=false, updatable=false)
	private Date updDt;
	private char reNotify = 'N';
	@Id
    private String symbol;
	@Id
    private String txid;
	private String txIdx;
	private String orderId;
	private char notifiable = 'Y';
	private long confirm;
	private int notiCnt;
	private String toAccount;
    private String toAddr;
	private String toTag;
	private double amount;
	private int uid;
	private String brokerId;
	private String fromAccount;
	private String fromAddr;
	private String fromTag;
	private String blockId;
	private long txTime;
	private double exptFee;
	private double realFee;
	private String errMsg;

//	public TbTrans() { }
	public TbTrans(SendRequest req) {
		this.symbol         = req.getSymbol();
		this.fromAccount 	= req.getFromAccount();
		this.fromAddr       = req.getFromAddress();
		this.toAddr         = req.getToAddress();
		this.amount         = req.getAmount();
	}

	public TbTrans(String symbol, String fromAccount, String fromAddr,
				   String toAccount, String toAddr, double amount) {
        this.symbol         = symbol;
        this.fromAccount 	= fromAccount;
        this.fromAddr       = fromAddr;
		this.toAccount 		= toAccount;
        this.toAddr         = toAddr;
        this.amount         = amount;
	}
	
}
