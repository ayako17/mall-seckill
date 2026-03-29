package com.seckill.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@Order(1) // ✅ 必须在 @Transactional（其顺序为 Integer.MAX_VALUE）之前执行
          // 这样在连接打开之前就能选择好数据源
public class ReadwriteSplittingAspect {

   @Around("execution(* com.seckill.service..*.*(..)) || " +
        "execution(* com.seckill.controller..*.*(..))")  // ✅ 添加 controller 层
public Object routeDataSource(ProceedingJoinPoint pjp) throws Throwable {
    MethodSignature sig = (MethodSignature) pjp.getSignature();
    Method method = sig.getMethod();

    // ✅ 检查实现类的注解，而不仅仅是接口（修复代理注解 bug）
    Transactional tx = method.getAnnotation(Transactional.class);
    if (tx == null) {
        try {
            Method implMethod = pjp.getTarget().getClass()
                    .getMethod(method.getName(), method.getParameterTypes());
            tx = implMethod.getAnnotation(Transactional.class);
        } catch (NoSuchMethodException ignored) {}
    }

    if (tx != null && tx.readOnly()) {
        DataSourceContextHolder.setSlave();
        log.debug("DataSource routed → SLAVE  ({})", method.getName());
    } else {
        DataSourceContextHolder.setMaster();
        log.debug("DataSource routed → MASTER ({})", method.getName());
    }

    try {
        return pjp.proceed();
    } finally {
        DataSourceContextHolder.clear();
    }
}
}