package com.method76.blockchain.node;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * @author 
 * @since 
 */
@SpringBootApplication
@EnableScheduling
public class BlockchainNodeApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlockchainNodeApiApplication.class
                , args);
    }
    
}
