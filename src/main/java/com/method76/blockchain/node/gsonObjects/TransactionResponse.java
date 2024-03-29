package com.method76.blockchain.node.gsonObjects;

import com.method76.blockchain.node.constants.abstracts.BlockchainConstant;
import com.method76.blockchain.node.domain.TbTrans;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data public class TransactionResponse implements BlockchainConstant {

    private char type;
    private char status;
    private String symbol;
    private int uid;
    private String brokerId;
    private String orderId;
    private String txid;
    private double amount;
    private long confirmation;
    private Long txtime;
    private String error;
    
    public TransactionResponse() {}
    
    public TransactionResponse(String symbol, char type, int uid, char status, String brokerId, String orderId, 
              String txid, double amount, long confirmation, Long txtime) {
      
        this.type = type;
        this.status = status;
        this.symbol = symbol;
        this.uid = uid;
        this.brokerId = brokerId;
        this.orderId = orderId;
        this.txid = txid;
        this.amount = amount;
        this.confirmation = confirmation;
        this.txtime = txtime;
    }
    
    public TransactionResponse(TbTrans datum) {
    	this.type = TYPE_TX_SEND;
    	this.status = datum.getErrMsg()!=null?STAT_FAILED
                :datum.getNotiCnt()==NOTI_CNT_PROGRESS?STAT_PROGRESS
                    :datum.getNotiCnt()==NOTI_CNT_FINISHED?STAT_COMPLETED:STAT_UNKNOWN;
        this.symbol = datum.getSymbol();
        this.uid = datum.getUid();
        this.brokerId = datum.getBrokerId();
        this.orderId = datum.getOrderId();
        this.txid = datum.getTxid();
        this.amount = datum.getAmount();
        this.confirmation = datum.getConfirm();
        this.txtime = datum.getTxTime();
    }
    
}
