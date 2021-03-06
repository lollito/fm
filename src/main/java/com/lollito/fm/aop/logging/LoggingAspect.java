package com.lollito.fm.aop.logging;


import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class LoggingAspect {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Pointcut("within(com.lollito.fm.repository.rest..*) || within(com.lollito.fm.service..*)")
    public void loggingPointcut() {
        // Method is empty as this is just a Poincut, the implementations are in the advices.
    }

    @Pointcut("within(com.lollito.fm.controller.rest..*)")
    public void loggingPointcutController() {
        // Method is empty as this is just a Poincut, the implementations are in the advices.
    }
    
    @AfterThrowing(pointcut = "loggingPointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
    	log.error("Exception in {}.{}() with cause = \'{}\' and exception = \'{}\'", joinPoint.getSignature().getDeclaringTypeName(),
    			joinPoint.getSignature().getName(), e.getCause() != null? e.getCause() : "NULL", e.getMessage(), e);

//            log.error("Exception in {}.{}() with cause = {}", joinPoint.getSignature().getDeclaringTypeName(),
//                joinPoint.getSignature().getName(), e.getCause() != null? e.getCause() : "NULL");
    }

    @Around("loggingPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        if (log.isDebugEnabled()) {
            log.debug("Enter: {}.{}() with argument[s] = {}", joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(), Arrays.toString(joinPoint.getArgs()));
        }
        try {
            Object result = joinPoint.proceed();
            if (log.isDebugEnabled()) {
                log.debug("Exit: {}.{}() with result = {}", joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(), result);
            }
            return result;
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument: {} in {}.{}()", Arrays.toString(joinPoint.getArgs()),
                    joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());

            throw e;
        }
    }
    
	@Around("loggingPointcutController()")
	public Object logAroundController(ProceedingJoinPoint joinPoint) throws Throwable {
		log.info("Enter: {}.{}() with argument[s] = {}", joinPoint.getSignature().getDeclaringTypeName(),
				joinPoint.getSignature().getName(), Arrays.toString(joinPoint.getArgs()));
		try {
			Object result = joinPoint.proceed();
			log.info("Exit: {}.{}() with result = {}", joinPoint.getSignature().getDeclaringTypeName(),
					joinPoint.getSignature().getName(), result);
			return result;
		} catch (IllegalArgumentException e) {
			log.error("Illegal argument: {} in {}.{}()", Arrays.toString(joinPoint.getArgs()),
					joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());

			throw e;
		}
	}
}
