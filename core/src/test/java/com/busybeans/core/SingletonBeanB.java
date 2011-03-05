package com.busybeans.core;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;

@Singleton
public class SingletonBeanB {

    public static final String SOMETHING = "Something ...";

    public String doSomething() {
        return SOMETHING;
    }

    @PostConstruct
    public void init() {
        System.out.println("Hello World B!");
    }

    @PreDestroy
    public void stop() {
        System.out.println("Goodbye World B!");
    }
}
