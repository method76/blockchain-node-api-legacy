package com.method76.blockchain.node.controllers;

import com.method76.blockchain.node.constants.abstracts.BlockchainConstant;
import com.method76.blockchain.node.gsonObjects.*;
import com.method76.blockchain.node.services.abstracts.BlockchainRpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


@Slf4j
@Controller
public class AdminController {

    @Autowired
    private BlockchainRpcService service;

    /**
     * 새 지갑 주소 생성
     */
    @RequestMapping(value="/", method=RequestMethod.GET)
    public String index() {
        log.info("index");
        return "html/index";
    }

}
