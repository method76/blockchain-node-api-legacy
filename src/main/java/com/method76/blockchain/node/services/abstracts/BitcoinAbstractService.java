package com.method76.blockchain.node.services.abstracts;

import com.google.gson.internal.LinkedTreeMap;
import com.method76.blockchain.node.constants.BitcoinConstant;
import com.method76.blockchain.node.domain.TbAddressBalance;
import com.method76.blockchain.node.domain.TbBlockchainMaster;
import com.method76.blockchain.node.domain.TbTrans;
import com.method76.blockchain.node.domain.primarykeys.PkSymbolAddr;
import com.method76.blockchain.node.gsonObjects.*;
import com.method76.blockchain.node.gsonObjects.bitcoin.*;
import com.method76.blockchain.node.services.interfaces.LockableWallet;
import com.method76.blockchain.node.services.interfaces.OwnChain;
import com.method76.blockchain.node.services.interfaces.SendManyWallet;
import com.method76.blockchain.node.utils.JsonRpcUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.ClientProtocolException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;


/**
 * Bitcoin 계열 공통 services
 * @author sungjoon.kim
 */
@Slf4j
public abstract class BitcoinAbstractService extends BlockchainRpcService
        implements BitcoinConstant, OwnChain, LockableWallet,
        SendManyWallet {
  
    public abstract String getRpcid();
    public abstract String getRpcpw();
    public abstract String getUseraccount();
    
    @Autowired
    private EntityManagerFactory emf;
    @Override public void beforeBatchSend() {}
    @Override public Set<String> getAllAddressSetFromNode() { return null; }
    @Override public List<String> getAllAddressListFromNode() { return null; }
    @Override public boolean syncWalletBalance(int uid) { return true; }

//    @Cacheable
    public DashboardResponse getDashboardData() {
        DashboardResponse ret = new DashboardResponse();
        long startCount = getBlockStartFrom();
        long bestCount = getBestBlockCount();
        long syncCount = -1;
        Optional<TbBlockchainMaster> item = blockchainMasterRepo.findById(getSymbol());
        if (item.isPresent()) {
            syncCount = item.get().getSyncHeight();
        }
        double balance = getTotalBalance();
        ret.setResult(startCount, syncCount, bestCount, balance);
        return ret;
    }

    public double getTotalBalance() {
        double ret = 0;
        try {
            String resStr = JsonRpcUtil.sendJsonRpcBasicAuth(getRpcurl(),
                    BitcoinConstant.METHOD_GETBALANCE, null,
                    getRpcid(), getRpcpw());
            logInfo(METHOD_GETBALANCE, resStr);
            BitcoinDoubleResponse res = gson.fromJson(resStr, BitcoinDoubleResponse.class);
            logDebug(METHOD_GETBALANCE, "res " + resStr);
            ret = res.getResult();
            if (res.getError()!=null) {
                logError(METHOD_GETBALANCE, res.getError());
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logError(METHOD_NEWADDR, e);
            return -1;
        }
        return ret;
    }

	/**
	 * 새 주소 발급
	 * @param
	 */
    @Transactional
    @Override public NewAddressResponse newAddress(PersonalInfoRequest req) {
	  
    	NewAddressResponse ret = getSavedAddress(req);
        if (ret.getResult().getAddress()!=null) { return ret; }
        ret.setResult(req);

	    // CASE2) 새 요청인 경우 생성 => param[0]: 어카운트ID
	    String[] params = new String[1];
	    params[0] = "\"" + getUseraccount() + "\"";
	    String resStr = null;
	    BitcoinStringResponse res = null;
	    
	    try {
	    	walletpassphraseshort();
    	    resStr = JsonRpcUtil.sendJsonRpcBasicAuth(getRpcurl(),
                    METHOD_NEWADDR, params, getRpcid(), getRpcpw());
    	    logInfo(METHOD_NEWADDR, resStr);
    	    res = gson.fromJson(resStr, BitcoinStringResponse.class);
    	    if (res.getError()!=null) {
                logError(METHOD_NEWADDR, res.getError());
                ret.setCode(CODE_FAIL_LOGICAL);
                return ret;
            }
    	    TbAddressBalance datum = new TbAddressBalance(
    	            ret.getResult().getUid(), getSymbol(),
                    null, res.getResult(), null,
                    ret.getResult().getBrokerId());
    	    addressBalanceRepo.save(datum);
    	    ret.setCode(CODE_SUCCESS);
    	    ret.getResult().setAddress(res.getResult());
    	    
	    } catch (Exception e) {
	    	e.printStackTrace();
  	        logError(METHOD_NEWADDR, e);
            ret.setCode(CODE_FAIL_LOGICAL);
            ret.setError(e.getMessage());
            return ret;
	    }
	    return ret;
	}
    
    @Override public boolean isSendAddrExists() {
    	String propSendAddr = getSendaddr();
        BitcoinGeneralResponse res = null;
        String[] params = new String[1];
        params[0] = "\"" + getUseraccount() + "\"";
        
        try {
            String resStr = JsonRpcUtil.sendJsonRpcBasicAuth(getRpcurl(),
            		METHOD_GETADDRESSES, params, getRpcid(), getRpcpw());
            res = gson.fromJson(resStr, BitcoinGeneralResponse.class);
            if (res.getError()!=null) {
                logError(METHOD_GETADDRESSES, res.getError());
                return false;
            } else {
                List<String> masteraddrs = (List<String>)res.getResult();
                if (masteraddrs.contains(propSendAddr)) {
                    return true;
                } else {
                    return false;  
                }
            }
        } catch (Exception e) {
            logError(METHOD_GETADDRESSES, e);
            return false;
        }
    }
    
	/**
	 * 주소 유효성 검증
	 */
    @Override public ValidateAddressResponse validateAddress(
            PersonalInfoRequest param) {
      
    	String resStr = null;
    	ValidateAddressResponse ret = new ValidateAddressResponse(param);
    	String[] params = new String[1];
    	params[0] = "\"" + ret.getResult().getAddress() + "\"";
    	ValidateAddress res = null;
    	try {
    	    resStr = JsonRpcUtil.sendJsonRpcBasicAuth(getRpcurl(),
                    METHOD_VALIDADDR, params,
    	    		getRpcid(), getRpcpw());
    	    res = gson.fromJson(resStr, ValidateAddress.class);
    	    logInfo(METHOD_VALIDADDR, resStr);
    	    if (res.getError()!=null) {
    	        // 정상: 응답갑 재확인 필요
                logError(METHOD_VALIDADDR, res.getError());
                ret.setCode(CODE_FAIL_LOGICAL);
                ret.setError(res.getError().getMessage());
                return ret;
            }
        } catch (Exception e) {
            logError(METHOD_VALIDADDR, e);
            ret.setCode(CODE_FAIL_LOGICAL);
            ret.setError(e.getMessage());
            return ret;
        }
    	ret.getResult().setValid(res.getResult().isIsvalid());
        return ret;
    }

    /**
     * 입출금 트랜잭션 조회
     */
    public GetTransaction getTransaction(String txid) {
        GetTransaction res = null;
    	String[] params = new String[1];
    	params[0] = "\"" + txid + "\"";
    	String resStr = null;
    	try {
    	    resStr = JsonRpcUtil.sendJsonRpcBasicAuth(getRpcurl(), METHOD_GETTX,
                    params, getRpcid(), getRpcpw());
    	    return gson.fromJson(resStr, GetTransaction.class);
    	} catch (Exception e) {
    	    res = new GetTransaction(new RpcError(-1, e.getMessage()));
    	    return res;   
    	}
    }
    
    /**
     * BTC sendmany 멀티 전송
     * (!) 수신자 주소가 같은 것이 있으면(중복) 에러가 발생하므로 중복 수신주소는 1개만 전송해야 함
     * 20개 정도 보내는 것이 안전할 듯
     * SendMany => params: fromAccount, to{address(string-base58):amount(double),,,,} => result(txid:string)
     *        {"1yeTWjh876opYp6R5VRj8rzkLFPE4dP3Uw":10,"1yeTWjh876opYp6R5VRj8rzkLFPE4dP3Uw":15}
     * [-8] Invalid parameter, duplicated address
     */
    @Transactional
    @Override public int sendMany(List<TbTrans> data) {
        int ret = 0; 
        Set<String> toAddrSet = new HashSet<>();
        Object[] params = new Object[2];
        params[0] = "\"" + getUseraccount() + "\"";
        StringBuilder paramsstr = new StringBuilder();
        paramsstr.append("{");
        DecimalFormat df = new DecimalFormat("#.########");
        // Iterate over send request count
        for (int i=0; i<data.size(); i++) {
            // 수신주소가 중복이 있으면 SENDMANY 실패한다.
            if (toAddrSet.contains(data.get(i).getToAddr())) {
                continue;
            }
            paramsstr.append("\"" + data.get(i).getToAddr() + "\":" 
            				+ df.format(data.get(i).getAmount()));
            if (i<data.size()-1) {
                paramsstr.append(",");
            }
        }
        paramsstr.append("}");
        params[1] = paramsstr.toString();
        
        // 노드에 전송 요청
        BitcoinStringResponse res = null;
        try {
            String resStr = JsonRpcUtil.sendJsonRpcBasicAuth(getRpcurl(),
                    METHOD_SENDMANY, params,
            		getRpcid(), getRpcpw());
            logSuccess(METHOD_SENDMANY, resStr);
            res = gson.fromJson(resStr, BitcoinStringResponse.class);
            if (res.getError()!=null) {
                logError(METHOD_SENDMANY, res.getError());
            }
        } catch (Exception e) {
            logError(METHOD_SENDMANY, e);
            return -2;
        }
        
        // 한번에 여러개 보냈으므로 데이터에 응답결과 가공
        for (TbTrans datum : data) {
            if (res.getError()!=null) {
                datum.setErrMsg("[" + res.getError().getCode() + "] " + res.getError().getMessage());
                logError(METHOD_SENDMANY, res.getError());
            } else {
                // 정상 응답
                datum.setTxid(res.getResult());
            }
        }
        
        return ret;
    }

    /**
     * 지갑 잠금 해제 for BATCH
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    @Override public boolean walletpassphrase() {
        return walletpassphrase(WALLET_UNLOCK_10SECS);
    }
    
    @Override public boolean walletpassphraseshort() {
        return walletpassphrase(WALLET_UNLOCK_SHORT);
    }
    
    /**
     * 지갑 잠금 해제 for 단건
     * @param interval
     * @return
     */
    @Override public boolean walletpassphrase(String interval) {
        // 1) WalletPassphrase => params: passphrase(string), seconds(number) => result: null 
    	BitcoinStringResponse res = null; 
        String[] params = new String[2];
        params[0] = "\"" + getPp() + "\"";
        params[1] = interval;
        try {
            String resStr = JsonRpcUtil.sendJsonRpcBasicAuth(getRpcurl(),
                    METHOD_WALLETPP, params,
            		getRpcid(), getRpcpw());
            logDebug(METHOD_WALLETPP, getRpcid() + " " + getRpcpw() + " " + resStr);
            res = gson.fromJson(resStr, BitcoinStringResponse.class);
            if (res.getError()!=null) {
                logError(METHOD_WALLETPP, res.getError());
                return false; 
            } else {
                logSuccess(METHOD_WALLETPP, "success");
                return true;
            }
        } catch (Exception e) {
            logError(METHOD_WALLETPP, e);
            return false;
        }
    }
    
    /**
     * 지갑 잠금
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    @Override public boolean walletlock() {
        // 1) WalletLock => params: null => result: null 
        BitcoinGeneralResponse res = null; 
        try {      
            String resStr = JsonRpcUtil.sendJsonRpcBasicAuth(getRpcurl(),
                    METHOD_WALLETLOCK, null,
            		getRpcid(), getRpcpw());
            res = gson.fromJson(resStr, BitcoinGeneralResponse.class);
            if (res.getError()!=null) {
                logError(METHOD_WALLETLOCK, res.getError());
                return false; 
            } else {
                logSuccess(METHOD_WALLETLOCK, "success");
                return true;
            }
        } catch (Exception e) {
            logError(METHOD_WALLETLOCK, e);
            return false;
        }
    }
    
    @Override
    public long getBestBlockCount() {
        BitcoinGeneralResponse res = null;
        try {
            res = gson.fromJson(JsonRpcUtil.sendJsonRpcBasicAuth(getRpcurl(),
            		METHOD_GETBLOCKCOUNT, null, getRpcid(), getRpcpw())
            		, BitcoinGeneralResponse.class);
            if (res.getError()!=null) {
                logError(METHOD_GETBLOCKCOUNT, res.getError());
                return 0;
            } else {
                return (long)(double)res.getResult();
            }
        } catch (Exception e) {
            logError(METHOD_GETBLOCKCOUNT, e);
            return 0;
        }
    }
    
    /**
     * listsinceblock으로 단순하게 대체 가능
     * METHOD_GETBLOCKHASH -> METHOD_LISTSINCEBLOCK
     */
    @Override public boolean openBlocksGetTxsThenSave() {
      
        TbBlockchainMaster master   = getCryptoMaster();
        long currentHeight      = master.getSyncHeight();
        long latestHeight       = master.getBestHeight();
        String senderaddr    	= getSendaddr();
        String currentBlockHash = null;
        
        // 동기화 할 블럭이 있으면
        if (currentHeight<latestHeight) {
          
            int txcount = 0; 
            logInfo("SYNC", currentHeight + "=>" + latestHeight);
            String[] params2 = new String[1];
            params2[0] = "" + currentHeight;
            BitcoinStringResponse res2 = null;
            try {
                // 블럭해시 가져오기
                res2 = gson.fromJson(
                        JsonRpcUtil.sendJsonRpcBasicAuth(getRpcurl(), METHOD_GETBLOCKHASH, params2, getRpcid(), getRpcpw())
                              , BitcoinStringResponse.class);
                if (res2==null) {
                    logError(METHOD_LISTSINCEBLOCK, "null");
                    return false;
                } else if (res2.getError()!=null) {
                    logError(METHOD_GETBLOCKHASH, res2.getError());
                    return false;
                }
            } catch (Exception e) {
                logError(METHOD_GETBLOCKHASH, e);
                e.printStackTrace();
                return false;
            }
            
            // 심볼 마스터 테이블에 블록 동기화 인덱스 저장
            currentBlockHash = res2.getResult();
            // 최근 동기화 블럭 인덱스 부터 최근까지 트랜잭션 반환
            String[] params3 = new String[1];
            params3[0] = "\"" + currentBlockHash + "\"";
            ListSinceBlock res3 = null;
            
            EntityManager em = emf.createEntityManager();
            EntityTransaction etx = em.getTransaction();
            etx.begin();
            
            if (currentBlockHash!=null) {
              
                String resStr = null;
                try {
                    // 내 트랜잭션만 반환
                    resStr = JsonRpcUtil.sendJsonRpcBasicAuth(getRpcurl(),
                    		METHOD_LISTSINCEBLOCK, params3, getRpcid(), getRpcpw());
                    res3 = gson.fromJson(resStr, ListSinceBlock.class);
                    if (res3==null) {
                        logError(METHOD_LISTSINCEBLOCK, "null");
                        if (etx.isActive()) { etx.rollback(); }
                        em.close();
                        return false;
                    } else if (res3.getError()!=null) {
                        logError(METHOD_LISTSINCEBLOCK, res3.getError().toString());
                        if (etx.isActive()) { etx.rollback(); }
                        em.close();
                        return false;
                    } else if (res3.getResult()==null) {
                    	logInfo("SYNC", "finished at " + latestHeight);
                        master.setSyncHeight(latestHeight);
                        em.merge(master);
                        etx.commit();
                        em.close();
                        return true;
                    }
                } catch (Exception e) {
                    logError(METHOD_LISTSINCEBLOCK, e);
                    e.printStackTrace();
                    if (etx.isActive()) { etx.rollback(); }
                    em.close();
                    return false;
                }
            } else {
                if (etx.isActive()) { etx.rollback(); }
                em.close();
                return false;
            }
            
            // 반환된 모든 WALLET트랜잭션 반복해서 SEND/RECEIVE인지 판단 한 뒤 각 테이블에 CONFIRMATION 수 업데이트
            ListSinceBlock.Result.Transaction[] txs = res3.getResult().getTransactions();
            if (txs==null || txs.length<1) {
            	logInfo("SYNC", "finished at " + latestHeight);
                master.setSyncHeight(latestHeight);
                em.merge(master);
                etx.commit();
                em.close();
            	return true; 
            }
            
            try {
                for (ListSinceBlock.Result.Transaction tx : txs) {
                    // 마지막 동기화 블럭 이후의 모든 (나의) 트랜잭션 반복
                    double amount    = Math.abs(tx.getAmount());
                    long time        = tx.getTime();
                    long confirm     = tx.getConfirmations();
                    String txid      = tx.getTxid();
                    String account   = tx.getAccount();
                    String addr      = tx.getAddress();
                    Long blockid     = getBlockHeight(tx.getBlockhash());
                    boolean failed   = false;

                    if (CATEGORY_SEND.equals(tx.getCategory())) {
                        // 1) 출금처리 START
                        double realfee = Math.abs(tx.getFee()/txs.length);
                        failed = tx.isAbandoned();
                        TbTrans datum = transactionRepo.
                                findFirstBySymbolAndTxidAndToAddrAndRegDtGreaterThanOrderByRegDtDesc
                                (getSymbol(), txid, tx.getAddress(), getLimitDate());
                        if (datum==null) {
                            // Todo: 에러처리 Insert
                        }
                        // 출금건: 마스터 어카운트에서 어드레스로. 여기서 account는 출금 account, address 는 수신 address 
//                        datum.setTxid(txid);
                        datum.setConfirm(confirm);
                        datum.setBlockId("" + blockid);
                        datum.setRealFee(realfee);
                        datum.setTxTime(time);
                        if (failed) {
                            if (res3.getError().getMessage()!=null) {
                                datum.setErrMsg("[" + res3.getError().getCode()
                                        + "] " + res3.getError().getMessage());
                            } else {
                                datum.setErrMsg(MSG_BLOCK_TX_FAIL);
                            }
                        }
                        em.merge(datum);
                        // 출금건 처리 END
                    } else if (CATEGORY_RECEIVE.equals(tx.getCategory())) {
                        // 2) 입금처리 START: 외부 주소에서 유저  어드레스로       
                        boolean istomaster = senderaddr!=null && senderaddr.equals(addr);
                        TbTrans datum = transactionRepo.
                                findFirstBySymbolAndTxidAndToAddrAndRegDtGreaterThanOrderByRegDtDesc(
                                        getSymbol(), txid, addr, getLimitDate());
                        if (datum==null) {
                            // find broker_id, uid, From addr 
                            List<TbAddressBalance> data = addressBalanceRepo.
                                    findBySymbolAndAddr(getSymbol(), tx.getAddress());
                            datum = new TbTrans(getSymbol(), null, null,
                                    account, addr, amount);
                            TbAddressBalance tbaddr = data.get(0);
                            datum.setTxid(txid);
                            datum.setUid(tbaddr.getUid());
                            datum.setBrokerId(tbaddr.getBrokerId());
                        }
                        datum.setConfirm(confirm);
                        datum.setBlockId("" + blockid);
                        datum.setTxTime(time);
                        if (istomaster) {
                            // 마스터주소로 입금건은 알리지 않음
                            datum.setNotifiable('N');
                        }
                        if (failed) {
                            // Todo: 에러 인서트
                            if (res3.getError().getMessage()!=null) {
                                datum.setErrMsg("[" + res3.getError().getCode() + "] " + res3.getError().getMessage());    
                            } else {
                                datum.setErrMsg(MSG_BLOCK_TX_FAIL);
                            }
                        }
                        em.merge(datum);
                        // 입금건 처리 END
                    }
                    // end of TXs loop
                    
                    txcount++;
                    if (txcount>99) {
                        master.setSyncHeight(blockid);
                        em.merge(master);
                        etx.commit();
                        etx.begin();
                        txcount = 0;
                    }
                }
                logInfo("SYNC", "finished at " + latestHeight);
                master.setSyncHeight(latestHeight);
                em.merge(master);
                etx.commit();
                em.close();
                return true;
            } catch (Exception e) {
            	logError("extractTxs", e);
                e.printStackTrace();
                if (etx.isActive()) { etx.rollback(); }
                em.close();
                return false;
            }
        } else {
            logDebug("SYNC", currentHeight + "=" + latestHeight + " skip");
            return true;
        }
    }
    
    /**
     * 
     * @param blockhash
     * @return
     */
    private Long getBlockHeight(String blockhash) {
        String[] params2 = new String[1];
        params2[0] = "\"" + blockhash + "\"";  
        GetBlock res = null;
        try {
            res = gson.fromJson(
                    JsonRpcUtil.sendJsonRpcBasicAuth(getRpcurl(), METHOD_GETBLOCK, params2, getRpcid(), getRpcpw())
                      , GetBlock.class);
          if (res.getError()!=null) {
              logError(METHOD_GETBLOCK, res.getError());
              return null;
          } else {
              return res.getResult().getHeight();
          }
      } catch (Exception e) {
          e.printStackTrace();
          logError(METHOD_GETBLOCK, e);
          return null;
      }
    }
    
    /**
     * 
     * @param datum
     * @return
     */
    @Override public boolean sendOneTransaction(TbTrans datum) {
        if (datum.getFromAccount()==null) {
            datum.setFromAccount(getUseraccount());
        }
        Object[] params  = new Object[3];
        DecimalFormat df = new DecimalFormat("#.########");
        params[0] = "\"" + datum.getFromAccount() + "\"";
        params[1] = "\"" + datum.getToAddr() + "\"";
        params[2] = df.format(datum.getAmount()); 
        // 노드에 전송 요청
        BitcoinStringResponse res = null;
        try {
            String resStr = JsonRpcUtil.sendJsonRpcBasicAuth(getRpcurl(),
                    METHOD_SENDFROMADDR, params, getRpcid(), getRpcpw());
            res = gson.fromJson(resStr, BitcoinStringResponse.class);
            if (res.getError()!=null) {
                // Ex) "error": {"code":-4, "message":"Transaction amount too small"}, {"code":-6, "message":"Account has insufficient funds"},
                logError(METHOD_SENDFROMADDR, res.getError());
                datum.setErrMsg("[" + res.getError().getCode() + "] " + res.getError().getMessage());
                datum.setTxid(res.getResult());;
                return false;
            } else {
                // ON SUCCESS
                datum.setTxid(res.getResult());
                return true;
            }
        } catch (Exception e) {
            // FAIL시 NOTIFY
            logError(METHOD_SENDFROMADDR, e);
            datum.setErrMsg("[-1]" + e.getMessage());
            return false;
        }
    }
    
    @Override public double getAddressBalance(String addr) {
    	List<TbAddressBalance> data = addressBalanceRepo.findBySymbolAndAddr(getSymbol(), addr);
    	if (data!=null && data.size()>0) { 
    		return data.get(0).getBalance(); 
    	} else {
    		return 0;
    	}
    }
    
    /**
     * BTC 계열에서는 ACCOUNT 그룹이므로 개별 주소 잔고는 의미 없음
     * METHOD_LISTBALANCES
     */
    @Transactional
    @Override public boolean syncWalletBalances() {
      
        BitcoinGeneralResponse res = null;
        try {
            String resStr = JsonRpcUtil.sendJsonRpcBasicAuth(getRpcurl(),
                    METHOD_LISTACCOUNTS, null, getRpcid(), getRpcpw());
            res = gson.fromJson(resStr, BitcoinGeneralResponse.class);
            if (res.getError()!=null) {
                logError(METHOD_LISTACCOUNTS, res.getError());
                return false;
            }
        } catch (Exception e) {
            logError(METHOD_LISTACCOUNTS, e);
            return false;
        }
        int totalcnt = 0, successcnt = 0;
        LinkedTreeMap result = (LinkedTreeMap) res.getResult();
        Iterator<String> keys = result.keySet().iterator();
        double totalbal = 0, amount = 0, senderbal = 0;
        while (keys.hasNext()) {
            totalcnt++;
            String accountname = (String)keys.next();
            amount = (double) result.get(accountname);
            if (getUseraccount().equals(accountname)) {
                senderbal += amount;
            }
            totalbal += amount;
        }

        BitcoinListAddressBalanceResponse res2 = null;
        try {
            String resStr = JsonRpcUtil.sendJsonRpcBasicAuth(getRpcurl(), METHOD_LISTBALANCES,
            		null, getRpcid(), getRpcpw());
            res2 = gson.fromJson(resStr, BitcoinListAddressBalanceResponse.class);
            if (res.getError()!=null) {
                logError(METHOD_LISTBALANCES, res.getError());
                return false;
            }
        } catch (Exception e) {
            logError(METHOD_LISTBALANCES, e);
            return false;
        }
        
        for (BitcoinListAddressBalanceResponse.AddressBalance item : res2.getResult()) {
            totalcnt++;
            amount = item.getAmount();
            List<TbAddressBalance> data = addressBalanceRepo.findBySymbolAndAddr(getSymbol(),
            		item.getAddress());
            if (data!=null && data.size()>0) {
            	TbAddressBalance datum = data.get(0);
            	datum.setBalance(amount);
            	addressBalanceRepo.save(datum);
            } else if (amount>0) {
                // 잔고가 있을 때만 고아 주소에 INSERT
            	PkSymbolAddr id = new PkSymbolAddr(getSymbol(), item.getAddress());
            }
            successcnt++;
        }
        return totalcnt==successcnt;
    }
    
    /**
     * 출금 CONFIRM수 변경 알림
     */
    @Transactional
    @Override public boolean updateTxConfirmCount() {
        boolean success = true;
        // 1) 출금 진행중인 건 조회: TXID가 있고 거래소에 알리지 않은 건
        List<TbTrans> data = getSendTXToUpdate();
        if (data!=null && data.size()>0) {
            for (TbTrans datum : data) {
                boolean notify = false;
                GetTransaction res = getTransaction(datum.getTxid());
                if (res.getError()!=null) {
                    String errMsg = "[" + res.getError().getCode() + "] " + res.getError().getMessage();
                    logError(METHOD_GETTX, errMsg);
                    if (res.getError().getCode()==-1) {
                        // 에러 코드가 -1이면 노드에러가 아니고 IO익셉션인 경우이므로 다음번에 재발송 될 가능성 있음
                        continue;
                    } else {
                        datum.setErrMsg(errMsg);
                        datum.setNotiCnt(NOTI_CNT_FINISHED);
                        notify = true;
                    }
                    success = false;
                } else {
                    if (res.getResult().getConfirmations()>=getMinconfirm()) {
                        // 완료처리해야 할 것 있는지 조회
                        datum.setNotiCnt(NOTI_CNT_FINISHED);
                        notify = true;
                    } else {
                        // 미완료 건 중 알림회수 0인 경우
                        if (datum.getNotiCnt()<NOTI_CNT_PROGRESS) {
                            datum.setNotiCnt(NOTI_CNT_PROGRESS);
                            notify = true;
                        }
                    }
                    datum.setConfirm(res.getResult().getConfirmations());
                    datum.setTxTime(res.getResult().getTime());
                    datum.setRealFee(Math.abs(res.getResult().getFee()));
                }
                if (notify) {
                    notifySendKafka(datum);
                }
                transactionRepo.save(datum);
            }
        }
        return success;
    }

}
