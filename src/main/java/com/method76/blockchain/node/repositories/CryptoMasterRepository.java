package com.method76.blockchain.node.repositories;

import com.method76.blockchain.node.domain.TbBlockchainMaster;
import org.springframework.data.repository.PagingAndSortingRepository;


public interface CryptoMasterRepository extends PagingAndSortingRepository
		<TbBlockchainMaster, String> {
}