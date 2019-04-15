package com.method76.blockchain.node.gsonObjects.bitcoin;

import lombok.Data;

@Data
public class GetWalletInfo {
    String walletname;
    int walletversion;
    double balance;
    double unconfirmed_balance;
    double immature_balance;
    int txcount;
    double paytxfee;
    boolean private_keys_enabled;
//    keypoololdest: 1555301795,
//    keypoolsize: 1000,
//    keypoolsize_hd_internal": 999,
//    hdseedid: "86281af34e015dde2ae2faa5779aa50c432a181e",
//    hdmasterkeyid: "86281af34e015dde2ae2faa5779aa50c432a181e",

}
