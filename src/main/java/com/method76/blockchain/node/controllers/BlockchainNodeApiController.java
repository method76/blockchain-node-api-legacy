package com.method76.blockchain.node.controllers;

import com.method76.blockchain.node.constants.abstracts.BlockchainConstant;
import com.method76.blockchain.node.gsonObjects.*;
import com.method76.blockchain.node.services.abstracts.BitcoinAbstractService;
import com.method76.blockchain.node.services.abstracts.BlockchainRpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


@Slf4j @Controller
@RequestMapping(value="/api/v1")
public class BlockchainNodeApiController implements BlockchainConstant {

    @Autowired
    private BitcoinAbstractService service;

    @ResponseBody
    @RequestMapping(value="/dashboard", method= RequestMethod.POST)
    public DashboardResponse dashboard() {
        return service.getDashboardData();
    }

    @RequestMapping(value="/getnewaccount", method= RequestMethod.POST) @ResponseBody
    public NewAddressResponse getnewaccount(@RequestBody PersonalInfoRequest param) {
        return null;
    }

    /**
     * 새 지갑 주소 생성
     */
    @RequestMapping(value="/getnewaddress", method= RequestMethod.POST) @ResponseBody
    public NewAddressResponse getnewaddress(@RequestBody PersonalInfoRequest param) {
        
        if (param.getUid()<0) {
            NewAddressResponse ret = new NewAddressResponse();
            ret.setCode(CODE_FAIL_PARAM);
            return ret;
        } else {
        	param.setBrokerId("KR00");
            return service.newAddress(param);
        }
    }
    
    /**
     * 지갑주소 유효성 검증
     */
    @RequestMapping(value="/validateaddress", method= RequestMethod.POST) @ResponseBody
    public ValidateAddressResponse validateaddress(@RequestBody PersonalInfoRequest param) {
    	
        if (param.getSymbol()==null || param.getAddress()==null) {
        	// 파라미터 오류
            ValidateAddressResponse ret = new ValidateAddressResponse();
            ret.setCode(CODE_FAIL_PARAM);
            return ret;
        } else if (!param.getAddress().equals(param.getAddress().trim())) {
        	// 공백이 있는 경우
    		ValidateAddressResponse ret = new ValidateAddressResponse(param);
    		ret.getResult().setValid(false);
    		return ret;
    	} else {
    		// 정상
            return service.validateAddress(param);
    	}
    }
    
    /**
     * 출금요청
     */
    @RequestMapping(value="/send", method= RequestMethod.POST) @ResponseBody
    public SendResponse send(@RequestBody SendRequest param) {

    	if (param.getSymbol()==null || param.getOrderId()==null || param.getUid()==0
        		|| param.getToAddress()==null || param.getAmount()<=0) {
            SendResponse res = new SendResponse();
            res.setCode(CODE_FAIL_PARAM);
            return res;
        } else {
            // 앞뒤 공백주소 방어로직
            param.setToAddress(param.getToAddress().trim());
            if ("".equals(param.getToTag())) { param.setToTag(null); }
            return service.requestSendTransaction(param);
        }
    }
    
    /**
     * 출금요청(내부용)
     */
    @RequestMapping(value="/sendfrom", method= RequestMethod.POST) @ResponseBody
    public SendResponse sendfrom(@RequestBody SendRequest param) {
    	if (param.getSymbol()==null || param.getOrderId()==null || param.getToAddress()==null
            || (param.getFromAddress()==null && param.getFromAccount()==null) || param.getAmount()<=0 
            || param.getBrokerId()==null || param.getBrokerId().length()!=4) {
            SendResponse res = new SendResponse();
            res.setCode(CODE_FAIL_PARAM);
            return res;
        } else {
            // 앞뒤 공백주소 방어로직
            if (param.getFromAccount()!=null && param.getFromAccount().length()>0) {
                param.setFromAccount(param.getFromAccount().trim());  
            }
            if (param.getFromAddress()!=null && param.getFromAddress().length()>0) {
                param.setFromAddress(param.getFromAddress().trim());  
            }
            if (param.getFromTag()!=null && param.getFromTag().length()>0) {
                param.setFromTag(param.getFromTag().trim());  
            }
            param.setToAddress(param.getToAddress().trim());
            if ("".equals(param.getToTag())) { param.setToTag(null); }
            return service.requestSendTransaction(param);
        }
    }
    
    /**
     * 사용자용) 토큰, 암호화폐, 원화, 잔고 조회
     */
    @RequestMapping(value="/getbalance", method= RequestMethod.POST) @ResponseBody
    public CryptoBalanceResponse getbalance(@RequestBody PersonalInfoRequest param) {
    	CryptoBalanceResponse res = new CryptoBalanceResponse();
        if (param.getUid()!=0) {
        	// UID로 조회할 때
        	double balance = service.getAddressBalance(
        			param.getAddress());
        	res.setResult(balance);
        } else {
            res.setCode(CODE_FAIL_PARAM);
        }
        return res;
    }

}
