package com.method76.blockchain.node;

import com.method76.blockchain.node.constants.abstracts.BlockchainConstant;
import com.method76.blockchain.node.interceptors.LogExecutionTime;
import com.method76.blockchain.node.services.abstracts.BlockchainRpcService;
import com.method76.blockchain.node.services.interfaces.OwnChain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@ConditionalOnProperty(value = "scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class BlockchainSyncScheduler implements BlockchainConstant {

    private final String TAG 	= "[SCHED]";
	private final int INTERVAL 	= 5000;
	private final int SEC_15   	= 3;       // 5000 * 3
	private final int MIN_1    	= 12;      // 5000 * 12
	private final int HOUR_3   	= 12 * 60 * 3; // 5000 * 12 * 60 * 3
	private int count;
	
	@Autowired
    private BlockchainRpcService service;

	/**
	 * 동기화 배치 스케줄러
	 */
	@LogExecutionTime
    @Scheduled(fixedDelay=INTERVAL)
    public void syncBlockTransactions() {
      
        boolean success1 = false;
        boolean success2 = false;
        
        // 1) 5초마다 => 암호화폐 기본 정보 업데이트 
        try {
            success1 = service.updateCryptoMaster();
            if (!success1) {
//              log.error(TAG + "[updateCryptoMaster] fail");
            }
        } catch (Exception e) {
//            log.error(TAG + "[updateCryptoMaster] " + e.getMessage());
        }
        
        // 2) 1분 마다 => 사용자 잔고 업데이트 (10초 지연)
        if (count%MIN_1==2) {
            try {
                boolean success = service.syncWalletBalances();
                if (!success) {
//                    log.error(TAG + "[syncWalletBalances] fail");
                }
            } catch (Exception e) {
                e.printStackTrace();
//                log.error(TAG + "[syncWalletBalances] "
//                				+ e.getMessage());
            }
        }
        
        // 3) 10초 마다 => 블록동기화 (5초 지연)
        // 블록을 열거나 내 트랜잭션 조회 API를 통해 거래소 주소 관련 TX들을 찾아내서 처리
        if (count%2==1) {
            if (service instanceof OwnChain) {
            	try {
                    if (!((OwnChain)service).openBlocksGetTxsThenSave()) {
//                        log.error(TAG + "[blockSync] fail");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
//                    log.error(TAG + "[blockSync] " + e.getMessage());
                }
            }
            
        }

        // 4) 15초 마다 => 전송대기건 전송 (5초지연)
        if (count%SEC_15==1) {
            try {
                if (!service.batchSendTransaction()) {
//                    log.error(TAG + "[batchSendTX] fail");
                }
            } catch (Exception e) {
                e.printStackTrace();
//                log.error(TAG + "[batchSendTX] " + e.getMessage());
            }
        }

        // 5) 15초 마다 => 입금/출금 컨펌 수 업데이트 (10초 지연)
        if (count%SEC_15==2) {
            try {
                success1 = service.updateSendConfirm();
            } catch (Exception e) { e.printStackTrace(); }
            try {
                success2 = service.updateReceiveConfirm();
            } catch (Exception e) { e.printStackTrace(); }
            if (!(success1 && success2)) {
//                log.error(TAG + "[updateConfirmation] fail");
            }
            // (1) [공통] 실패한 트랜잭션 알림 - 전체 한번만 실행해야 하는 건 BTC에다 구현함
            success1 = service.notifyTXsFailedNotNotified();
//            if (!success1) { log.error(TAG + "[notifyfailed] failed"); }
            // (2) 거래소에 KAFKA 알림 실패한 트랜잭션 재송신: 공통
            success2 = service.renotify();
//            if (!success2) { log.error(TAG + "[renotify] failed"); }
        }
        count++;
        if (count>=HOUR_3) { count = 0; }
    }
    
}

