package com.method76.blockchain.node.repositories;

import com.method76.blockchain.node.domain.TbBlockchainMaster;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;


public interface BlockchainMasterRepository extends
		CrudRepository <TbBlockchainMaster, String> {
}