package com.buzybeans.core.beans;

import com.buzybeans.core.classloading.BuzybeansClassloader;
import com.buzybeans.core.jpa.JPAService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.ejb.Singleton;
import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.persistence.Entity;

public class EJBApplication {

    private BuzybeansClassloader classLoader;

    private List<Bean<?>> beans;

    private Collection<Class<?>> entities;

    private JPAService jpaService;

    public EJBApplication() {
        classLoader = new BuzybeansClassloader(getClass().getClassLoader());
        beans = new ArrayList<Bean<?>>();
        entities = new ArrayList<Class<?>>();
        jpaService = new JPAService(classLoader);
    }


    public void start() {
        jpaService.setEntities(entities);
        jpaService.start();
        for (Bean<?> bean : beans) {
            if (bean.isStartable()) {
                bean.start();
            }
        }
    }

    public void stop() {
        for (Bean<?> bean : beans) {
            bean.stop();
        }
        jpaService.stop();
    }

    public Class<?> getClass(String name) throws ClassNotFoundException {
        return classLoader.loadClass(name);
    }

    public BuzybeansClassloader getClassLoader() {
        return classLoader;
    }

    public JPAService getJpaService() {
        return jpaService;
    }

    public Collection<Class<?>> getApplicationClasses() {
        return Collections.unmodifiableList(classLoader.getClasses());
    }

    public void initManageable() {
        Collection<Class<?>> classes = getApplicationClasses();
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(Stateful.class)) {
                beans.add(Bean.newBean(Bean.Type.STATEFUL, clazz, this));
            }
            if (clazz.isAnnotationPresent(Stateless.class)) {
                beans.add(Bean.newBean(Bean.Type.STATELESS, clazz, this));
            }
            if (clazz.isAnnotationPresent(Singleton.class)) {
                beans.add(Bean.newBean(Bean.Type.SINGLETON, clazz, this));
            }
            if (clazz.isAnnotationPresent(Entity.class)) {
                entities.add(clazz);
            }
        }
    }
}
