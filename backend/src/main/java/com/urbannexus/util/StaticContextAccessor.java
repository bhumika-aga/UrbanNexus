/*
 * Copyright (c) 2026 Bhumika Agarwal
 */

package com.urbannexus.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class StaticContextAccessor implements ApplicationContextAware {
    
    private static ApplicationContext context;
    
    public static <T> T getBean(Class<T> beanClass) {
        if (context == null) {
            throw new IllegalStateException("ApplicationContext is not initialized");
        }
        return context.getBean(beanClass);
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }
}
