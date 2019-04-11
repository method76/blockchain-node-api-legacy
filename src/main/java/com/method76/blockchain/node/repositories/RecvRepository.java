package com.method76.blockchain.node.repositories;

import com.method76.blockchain.node.domain.TbTrans;
import com.method76.blockchain.node.domain.primarykeys.PkSymbolTxidToAddr;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Date;
import java.util.List;

public interface RecvRepository extends PagingAndSortingRepository
        <TbTrans, PkSymbolTxidToAddr> {
	
    // 1) 블록에서 발견한 입금 건이 입금 테이블에 있는지 조회
    TbTrans findFirstBySymbolAndTxidAndToAddr(String symbol, String txid, String toaddr);
	// 2) 입금 confirm 업데이트 대상 조회
    List<TbTrans> findBySymbolAndErrMsgIsNullAndNotiCntLessThanAndRegDtGreaterThan(String symbol, int notiCnt, Date regDt);
    // 3) 입금 실패 알림 대상 목록
    List<TbTrans> findByErrMsgIsNotNullAndNotiCntLessThanAndRegDtGreaterThan
            (int notiCnt, Date regDt);
    // 4) 수기 처리한 재 알림 대상 트랜잭션들 조회
    List<TbTrans> findByReNotify(char reNotify);
    
    List<TbTrans> findByUid(int uid);
    
}