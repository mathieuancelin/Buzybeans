package com.busybeans.core;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Singleton
@Startup
public class SingletonBeanA {

    @EJB
    private SingletonBeanB beanB;

    @PersistenceContext
    private EntityManager em;

    public String doSomething() {
        return beanB.doSomething();
    }

    public EntityManager getEm() {
        return em;
    }

    @PostConstruct
    public void init() {
        System.out.println("Hello World A!");
        EJBTest.actions ++;
    }

    @PreDestroy
    public void stop() {
        System.out.println("Goodbye World A!");
        EJBTest.actions ++;
    }
}
