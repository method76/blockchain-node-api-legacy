package com.method76.blockchain.node.constants;

public interface BitcoinConstant {
    String METHOD_CREATEWALLET     = "createwallet";
    String METHOD_NEWADDR          = "getnewaddress";
    String METHOD_VALIDADDR        = "validateaddress";
    String METHOD_GETWALLETINFO    = "getwalletinfo";
    String METHOD_GETADDRESSES     = "getaddressesbyaccount";
    String METHOD_LISTACCOUNTS     = "listaccounts";
    String METHOD_LISTBALANCES     = "listreceivedbyaddress";
    String METHOD_GETBALANCE       = "getbalance";
    String METHOD_SENDFROMADDR     = "sendfrom";
    String METHOD_SENDMANY         = "sendmany";
    String METHOD_GETTX            = "gettransaction";
    String METHOD_GETBLOCKCOUNT    = "getblockcount";
    String METHOD_GETBLOCKHASH     = "getblockhash";
    String METHOD_GETBLOCK         = "getblock";
    String METHOD_LISTSINCEBLOCK   = "listsinceblock";
    String METHOD_WALLETPP         = "walletpassphrase";
    String METHOD_WALLETLOCK       = "walletlock";    
}
