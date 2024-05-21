package com.jh.restaurantreservationpj.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Slf4j
@Component
public class LogAspect {

    // 모든 메소드 포인트컷
    @Pointcut("execution(* com.jh.restaurantreservationpj..*(..))")
    public void all(){}

    // Controller클래스의 모든 메소드 포인트컷
    @Pointcut("execution(* com.jh.restaurantreservationpj..*Controller.*(..))")
    public void controller(){}

    // Service클래스의 모든 메소드 포인트컷
    @Pointcut("execution(* com.jh.restaurantreservationpj..*Service.*(..))")
    public void service(){}

    // Service 또는 Controller의 메소드 실행 전 로그 표시
    @Before("controller() || service()")
    public void beforeLog(JoinPoint joinPoint){
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        log.info("{}메소드 실행", method.getName());

        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if(arg != null){
                log.info("파라미터 타입 = {}", arg.getClass().getSimpleName());
                log.info("파라미터 값 = {}", arg);
            }
        }
    }

    // 모든 메소드에서 예외 발생시 로그 표시
    @AfterThrowing(pointcut = "all()", throwing = "ex")
    public void afterThrowingLog(JoinPoint joinPoint, Throwable ex){
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        log.error("Exception in method = {}", method.getName());
        log.error("Exception message = {}", ex.getMessage());
    }
}
