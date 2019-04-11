package com.method76.blockchain.node.repositories;

import com.method76.blockchain.node.domain.TbAddressBalance;
import com.method76.blockchain.node.domain.primarykeys.PkSymbolUid;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface AddressBalanceRepository extends PagingAndSortingRepository
        <TbAddressBalance, PkSymbolUid> {
	
    List<TbAddressBalance> findBySymbol(String symbol);
    List<TbAddressBalance> findBySymbolAndBalanceGreaterThanEqual(String symbol, double balance);
    List<TbAddressBalance> findBySymbolAndAddrIn(String symbol, List<String> addr);
    List<TbAddressBalance> findByUid(int uid);
    List<TbAddressBalance> findBySymbolAndUidAndBrokerId(String symbol, int uid, String brokerId);
    List<TbAddressBalance> findBySymbolAndAddr(String symbol, String addr);
    
    @Query("select addr from TbAddressBalance where symbol = ?1")
    List<String> findAddrBySymbol(String symbol);
    
}