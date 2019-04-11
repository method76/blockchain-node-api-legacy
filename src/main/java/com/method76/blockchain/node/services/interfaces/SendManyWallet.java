package com.method76.blockchain.node.services.interfaces;

import com.method76.blockchain.node.domain.TbTrans;

import java.util.List;

public interface SendManyWallet {
	int sendMany(List<TbTrans> data);
}