package com.method76.blockchain.node.services.interfaces;

import java.util.List;
import java.util.Set;

public interface OwnChain {
    boolean openBlocksGetTxsThenSave();
    long getBestBlockCount() throws Exception;
    long getBlockStartFrom();
    long getMinconfirm();
    List<String> getAllAddressListFromNode();
    Set<String> getAllAddressSetFromNode();
}