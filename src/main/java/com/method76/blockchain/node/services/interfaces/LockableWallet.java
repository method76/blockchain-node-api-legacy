package com.method76.blockchain.node.services.interfaces;

public interface LockableWallet {

    boolean walletpassphrase();
    boolean walletpassphraseshort();
    boolean walletpassphrase(String interval);
    boolean walletlock();
    String getPassphrase();
    
}