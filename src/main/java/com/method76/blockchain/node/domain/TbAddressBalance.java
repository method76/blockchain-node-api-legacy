package com.method76.blockchain.node.domain;


import com.method76.blockchain.node.domain.primarykeys.PkSymbolUid;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@Entity
@IdClass(PkSymbolUid.class)
public class TbAddressBalance implements Serializable {

	private static final long serialVersionUID = 5127319640626022175L;
	@Column(columnDefinition="TIMESTAMP", insertable=false, updatable=false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date regDt;
	@Id
	private int uid;
	@Id
	private String symbol;
	private String addr;
	private double balance;
	private String account;
	private String tag;
	private String brokerId;

	public TbAddressBalance(int uid, String symbol, String account, String addr,
							String tag, String brokerId) {
		this.symbol = symbol;
		this.uid = uid;
		this.account = account;
		this.addr = addr;
		this.tag = tag;
		this.brokerId = brokerId;
	}

	@Override public String toString() {
		return "[" + symbol + "] '" + uid + "'(" + addr + ") balance is " + balance;
	}

}

