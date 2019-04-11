package com.method76.blockchain.node.repositories;

import com.method76.blockchain.node.domain.TbTrans;
import com.method76.blockchain.node.domain.primarykeys.PkSymbolOrderId;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Date;
import java.util.List;

public interface TransactionRepository extends PagingAndSortingRepository
        <TbTrans, PkSymbolOrderId> {
	
    // 1) 블록에서 발견한 관련 출금 건이 출금 테이블에 있는지 조회
    TbTrans findFirstBySymbolAndTxidAndToAddrAndRegDtGreaterThanOrderByRegDtDesc
    (String symbol, String txid, String toAddr, Date regDt);
    // 2) 최근 전송 Fee 조회
    TbTrans findFirstBySymbolAndRealFeeGreaterThanOrderByRegDtDesc(String symbol,
                                                                  double realFee);
	// 3) 출금 처리 대상 조회 => TXID가 없고 에러 메시지가 없는 건
    List<TbTrans> findBySymbolAndTxidIsNullAndErrMsgIsNullAndRegDtGreaterThan
    (String symbol, Date regDt);
    // 4) 출금 confirm 업데이트 대상 조회
    List<TbTrans> findBySymbolAndTxidIsNotNullAndErrMsgIsNullAndNotiCntLessThanAndRegDtGreaterThan
    (String symbol, int notiCnt, Date regDt);
    // 5) 출금 실패 알림 대상 조회
    List<TbTrans> findByErrMsgIsNotNullAndNotiCntLessThanAndRegDtGreaterThan(int notiCnt,
                                                                            Date regDt);
    // 6) 수기 처리한 재 알림 대상 트랜잭션들 조회
    List<TbTrans> findByReNotify(char reNotify);

    // 7) 시스템 내부 송금건 추출
    List<TbTrans> findBySymbolAndUidAndBrokerIdAndTxidIsNotNullAndRegDtGreaterThan(
            String symbol, int uid, String brokerId, Date regDt);

    // 잔고 수거 요청건 조회
    TbTrans findFirstBySymbolAndFromAddrAndToAddrAndRegDtGreaterThanOrderByRegDtDesc
    (String symbol, String fromAddr, String toAddr, Date regDt);
    
    List<TbTrans> findByUid(int uid);
    List<TbTrans> findByOrderIdOrderByRegDtDesc(String orderId);
    
}