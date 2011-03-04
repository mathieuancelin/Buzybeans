package com.busybeans.core;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Singleton
@Startup
public class SingletonBean {

    @PostConstruct
    public void init() {
        System.out.println("Hello World!");
        EJBTest.actions ++;
    }

    @PreDestroy
    public void stop() {
        System.out.println("Goodbye World!");
        EJBTest.actions ++;
    }
}
