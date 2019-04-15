package com.method76.blockchain.node.services.abstracts;

import com.google.gson.Gson;
import com.method76.blockchain.node.constants.abstracts.BlockchainConstant;
import com.method76.blockchain.node.domain.TbAddressBalance;
import com.method76.blockchain.node.domain.TbBlockchainMaster;
import com.method76.blockchain.node.domain.TbTrans;
import com.method76.blockchain.node.gsonObjects.*;
import com.method76.blockchain.node.gsonObjects.bitcoin.RpcError;
import com.method76.blockchain.node.repositories.AddressBalanceRepository;
import com.method76.blockchain.node.repositories.BlockchainMasterRepository;
import com.method76.blockchain.node.repositories.TransactionRepository;
import com.method76.blockchain.node.services.interfaces.OwnChain;
import com.method76.blockchain.node.services.interfaces.LockableAddress;
import com.method76.blockchain.node.services.interfaces.LockableWallet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 
 * @author sungjoon.kim
 */
@Slf4j
public abstract class BlockchainRpcService implements BlockchainConstant {

	public final String TAG = "[" + getSymbol() + "]";

    // wallet properties
    public abstract String getSymbol();
    public abstract String getRpcurl();
    public abstract String getOwneraddress();
    public abstract int getDecimals();
    public abstract boolean isSendAddrExists();
    public abstract Object getTransaction(String txid);
    public abstract double getAddressBalance(String address);

    public abstract boolean syncWalletBalance(int uid);
    public abstract boolean syncWalletBalances();
    public abstract double getMinamtgather();

    // open API functions for exchange
    public abstract NewAddressResponse newAddress(PersonalInfoRequest req);
    public abstract ValidateAddressResponse validateAddress(PersonalInfoRequest param);
    // sync tx
    public abstract void beforeBatchSend();
    public abstract boolean sendOneTransaction(TbTrans datum);
    public abstract DashboardResponse getDashboardData();
    public abstract boolean updateTxConfirmCount();
    public abstract double getTotalBalance();

    // 입출금 테이블
    @Autowired
    protected AddressBalanceRepository addressBalanceRepo;
    @Autowired
    protected TransactionRepository transactionRepo;
    @Autowired
    protected BlockchainMasterRepository blockchainMasterRepo;
    @Autowired
    private EntityManagerFactory emf;
    protected Gson gson = new Gson();

    public List<String> getAllAddressListFromDB() {
        return addressBalanceRepo.findAddrBySymbol(getSymbol());
    }

    /**
     * 공통 배치 출금 
     * @return
     */
    public boolean batchSendTransaction() {
        
        // 전처리 필요 시
        beforeBatchSend();
        List<TbTrans> data = getToSendList();
        
        if (data==null || data.size()<1) {
            return true;
        } else {
            int successcount = 0;
            int datasize = data.size();
            boolean success = false;
            // 1) UNLOCK NODE
            if (this instanceof LockableWallet && this instanceof OwnChain) {
                success = ((LockableWallet)this).walletpassphrase();
                if (!success) { return false; }
            }
            
            EntityManager em  = emf.createEntityManager();
            EntityTransaction etx = em.getTransaction();
            
            // Todo: WHAT IF SENDMANY WALLET?
            for (TbTrans datum : data) {
            	etx.begin();
                // 2) SEND
                if (this instanceof LockableAddress) {
                    if (datum.getFromAddr()!=null && datum.getFromAddr().length()>0) {
                        success = ((LockableAddress)this)
                        		.walletpassphraseWithAddress(datum.getFromAddr());
                        if (!success) { 
                        	datum.setErrMsg(MSG_UNLOCK_FAIL);
                        	em.merge(datum);
                        	etx.commit();
                        	continue; 
                    	}
                    }
                }
                try {
                    success = sendOneTransaction(datum);
                    if (datum.getErrMsg()!=null && datum.getErrMsg().length()>0) {
                    	// FAIL
                        datum.setErrMsg(MSG_SEND_FAIL);
                    }
                    if (success) { successcount++; }
                    em.merge(datum);
                    etx.commit();
                    
                    // 토큰 구매용 출금 요청인 경우
                    etx.begin();
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
            em.close();
            data.clear();
            data = null;
            if (this instanceof LockableWallet && this instanceof OwnChain) {
                // 4) LOCK NODE
                success = ((LockableWallet)this).walletlock();
            }
            return (successcount==datasize);
        }
    }

    @Transactional
    public SendResponse requestSendTransaction(SendRequest req) {
      
        if (this instanceof BitcoinAbstractService && req.getFromAccount()==null) {
            // 2018-07-25 sungjoon.kim 비트계열 출금어카운트를 유저어카운트로 변경
            req.setFromAccount(((BitcoinAbstractService)this).getUseraccount());
        } else if (req.getFromAddress()==null) {
            req.setFromAddress(this.getOwneraddress());
        }
        if (SYMBOL_BCD.equals(getSymbol())) {
            // sungjoon) BCD는 소수점 5자리 이하 송금 시 에러 발생하여 긴급 조치
            req.setAmount((double)Math.floor(req.getAmount()*10000d)/10000d);
        }
        SendResponse ret = new SendResponse();
        ret.setResult(req);
        
        try {
            TbTrans datum = new TbTrans(req);
            transactionRepo.save(datum);
        } catch (Exception e) {
            e.printStackTrace();
            ret.setCode(CODE_FAIL_LOGICAL);
            ret.setError(e.getMessage());
        }
        return ret;
    }
    
    /**
     * 출금내역) 
     * 1) 거래소의 경우  KAFKA 객체로 변환하여 전송
     * 2) 프라이빗 세일의 경우 구매상태 변경
     * @param send
     * @return
     */
    public boolean notifySendKafka(TbTrans send) {
    	
        if (send==null) { return false; }
        if (send.getNotifiable()=='N' || send.getUid()<0) {
            // 알리지 말아야할 TX에 대한 방어코드
            send.setNotifiable('N');
            return true;
        } else {
        	// 1. 거래소의 경우 KAFKA로 알림
//            TransactionResponse item = WalletUtil.convertTbSendToResponse(datum);
//            sender.send(TopicId.walletTransmit(datum.getBrokerId()), item);
			// 2. 모바일 월렛의 경우 FCM 알림
//        	sendFcm(send.getIntent(), ldatum.getStatus(), ldatum.getPayAmount()
//	        					, ldatum.getTokenAmount(), send.getFromAddr(), SYMBOL_CPD);
		}
        return true;
    }
    
    
    /**
     * 입금내역  KAFKA 객체로 변환하여 전송
     * @param recv
     * @return
     */
    public boolean notifyRecvKafka(TbTrans recv) {
      
        if (recv==null) { return true; }
        if (recv.getBrokerId()==null || recv.getNotifiable()=='N'
                    || BROKER_ID_SYSTEM.equals(recv.getBrokerId()) 
                    || COINBASE_NAME.equals(recv.getFromAddr())) {
            // 알리지 말아야할 TX에 대한 방어코드
            recv.setNotifiable('N');
        } else {
        	
        	// 거래소의 경우 KAFKA로 알림
//            TransactionResponse item = new TransactionResponse(datum);
//            sender.send(TopicId.walletTransmit(datum.getBrokerId()), item);
        }
        return true;
    }
    
    /**
     * 1) 출금 처리해야할 건 조회
     * @return
     */
    public List<TbTrans> getToSendList() {
        return transactionRepo.findBySymbolAndTxidIsNullAndErrMsgIsNullAndRegDtGreaterThan
                (getSymbol(), getLimitDate());
    }
    /**
     * 2) 출금 진행중인 건 중에 완료처리해야 할 것 있는지 조회
     * @return
     */
    public List<TbTrans> getSendTXToUpdate() {
        return getSendTXToUpdate(getSymbol());
    }
    
    /**
     * 최종 confirm 완료되지 않은 트랜잭션 목록
     * @param symbol
     * @return
     */
    public List<TbTrans> getSendTXToUpdate(String symbol) {
        return transactionRepo.findBySymbolAndTxidIsNotNullAndErrMsgIsNullAndNotiCntLessThanAndRegDtGreaterThan
                    (symbol, NOTI_CNT_FINISHED, getLimitDate());
    }
    
    /**
     * 입출금 실패 건 중 알리지 않은 건이 있는지 조회
     * @return
     */
    @Transactional
    public boolean notifyTXsFailedNotNotified() {
      
        boolean success1 = true, success2 = true;
        // 1) 출금 실패 건 알림
        List<TbTrans> data1 = transactionRepo.findByErrMsgIsNotNullAndNotiCntLessThanAndRegDtGreaterThan
                    (NOTI_CNT_FINISHED, getLimitDate());
        if (data1!=null && data1.size()>0) {
            try {
                for (TbTrans datum : data1) {
                    if (datum.getBrokerId()==null || datum.getNotifiable()=='N' 
                              || BROKER_ID_SYSTEM.equals(datum.getBrokerId())) {
                        // 출금 실패 건 중: 브로커ID가 없거나 알리지 않아야할 수신건, 시스템에서 처리하는 출금건인 경우
                        datum.setNotifiable('N');
                    } else {
                        // 출금 실패 건: 일반
//                        TransactionResponse item = WalletUtil.convertTbSendToResponse(datum);
//                        sender.send(TopicId.walletTransmit(datum.getBrokerId()), item);                           
                    }
                    datum.setNotiCnt(NOTI_CNT_FINISHED);
                }
                transactionRepo.saveAll(data1);
            } catch (Exception e) {
                success1 = false;
                e.printStackTrace();
            }
        }
        
        return (success1 && success2);
    }
    
    /**
     * insert or update
     * @return
     */
    @Transactional
    public boolean syncBlockchainMasterTable() {
    	
        TbBlockchainMaster master = getCryptoMaster();
        long blockstartfrom = 0, latestblock = 0;
        try {
        	master.setDecimals(getDecimals());
        	if (isSendAddrExists()) {
            	master.setSendMastAddr(getOwneraddress());
            }
        	
        	if (this instanceof OwnChain) {
	            blockstartfrom = master.getSyncHeight();
	            if (blockstartfrom<((OwnChain)this).getBlockstartfrom()) {
	                blockstartfrom = ((OwnChain)this).getBlockstartfrom();
	            }
	            if (blockstartfrom<1) {
	            	logError("getBlockstartfrom", "couldn't find blockstartfrom property");
	            	return false;
	        	}
	            // get latest blocknum from chain
	            latestblock = ((OwnChain)this).getBestBlockCount();
	            if (latestblock>0) {
	                if (blockstartfrom>latestblock) {
	                    logError("getBestBlockCount", "curr sync height is bigger than bestchain");
	                } else {
	                	master.setBestHeight(latestblock);
	                }
	            } else {
	                master.setBestHeight(blockstartfrom);
	            }
        	}
            
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
        // get latest actual fee
        TbTrans datum = transactionRepo.findFirstBySymbolAndRealFeeGreaterThanOrderByRegDtDesc
        		(getSymbol(), 0);
        double actualFee = 0;
        if (datum!=null && datum.getRealFee()>0) {
            actualFee = datum.getRealFee();
            master.setLastTxFee(actualFee); // 최근 송금 피
        }
        blockchainMasterRepo.save(master);
        return true;
    }
    
    /**
     * DB에서 마스터 테이블 조회하는 경우
     * @return
     */
    @Transactional
    public TbBlockchainMaster getCryptoMaster() {
        Optional<TbBlockchainMaster> obj = blockchainMasterRepo.findById(getSymbol());
        TbBlockchainMaster master = null;
        if (obj.isPresent()) {
            master = obj.get();
        } else if (this instanceof OwnChain) {
            master = new TbBlockchainMaster(getSymbol(), getOwneraddress(), ((OwnChain)this).getBlockstartfrom()
            		, ((OwnChain)this).getBlockstartfrom());
        } else {
        	master = new TbBlockchainMaster(getSymbol(), getOwneraddress());
        }
        return master;
    }
    
    public NewAddressResponse getSavedAddress(PersonalInfoRequest req) {
    	NewAddressResponse ret = new NewAddressResponse();
    	ret.setResult(req);
        List<TbAddressBalance> result = addressBalanceRepo.findBySymbolAndUidAndBrokerId(getSymbol(), req.getUid(), req.getBrokerId());
        // CASE1) TABLE에 동일한 요청이 있으면 기존 생성값 리턴
        if (!result.isEmpty()) {
            if (result.size()==1) {
                logDebug("getSavedAddress", "address exists " + result);
                ret.getResult().setAddress(result.get(0).getAddr());
            } else if (result.size()>1) {
                logError("getSavedAddress", "multiple address exists " + result);
                ret.getResult().setAddress(result.get(0).getAddr());
            }
        }
        return ret;
    }
    
    /**
     * 
     * query: update TB_SEND set re_notify = 'Y' 
     * @return
     */
    @Transactional
    public boolean renotify() {
      
    	int renotiCnt = 0, successcnt = 0;
    	
    	try {
	        
	        List<TbTrans> data1 = transactionRepo.findByReNotify('Y');
	        if (data1!=null && data1.size()>0) {
	            renotiCnt += data1.size();
	            for (TbTrans datum : data1) {
	                datum.setReNotify('N');
	                notifySendKafka(datum);
	                successcnt++;
	            }
	            transactionRepo.saveAll(data1);
	        }
	        if (renotiCnt>0) {
	            logInfo("reNotify", "success/count " + successcnt + "/" + renotiCnt);
	        }
    	} catch (Exception e) { e.printStackTrace(); }
        return (successcnt==renotiCnt);
    }
    
    protected Date getLimitDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, SEND_UNSENT_TX_LIMIT_DATE);
        return cal.getTime();
    }
    
    protected Date getGatherLimitHour() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, SEND_GATHER_LIMIT_HOUR);
        return cal.getTime();
    }
    
    protected Date getSendRequestLimitDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, SEND_TX_SEARCH_LIMIT_DATE);
        return cal.getTime();
    }
    
    public void logInfo(String method, String res) {
        log.info("[" + getSymbol() + "][" + method + "] " + res);
    }
    
    public void logSuccess(String method, String res) {
        log.info("[" + getSymbol() + "][" + method + "] " + res);
    }
    
    public void logDebug(String method, String res) {
        log.debug("[" + getSymbol() + "][" + method + "] " + res);
    }
    
    public void logWarn(String method, String res) {
        log.warn("[" + getSymbol() + "][" + method + "] " + res);
    }
    
    public void logError(String method, Exception e) {
        log.error("[" + getSymbol() + "][" + method + "] " + e.getMessage());
//        e.printStackTrace();
    }
    
    public void logError(String method, RpcError e) {
        log.error("[" + getSymbol() + "][" + method + "] [" + e.getCode() + "] " 
            + e.getMessage());
    }
    
    public void logError(String method, String msg) {
        log.error("[" + getSymbol() + "][" + method + "] " + msg);
    }
    
}