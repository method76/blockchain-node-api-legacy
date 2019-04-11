package com.method76.blockchain.node.interceptors;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;


@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Around("@annotation(LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long starttime = System.currentTimeMillis();
        Object ret = joinPoint.proceed();
        log.info("[APM] " + joinPoint.getSignature().getName() + " executed in "
                        + (System.currentTimeMillis() - starttime) + "ms");
        return ret;
    }

}