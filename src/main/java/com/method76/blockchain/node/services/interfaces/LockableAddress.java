package com.method76.blockchain.node.services.interfaces;

public interface LockableAddress {

    boolean walletpassphraseWithAddress(String address);
    boolean walletlock(String address);
    String getPp();
    
}