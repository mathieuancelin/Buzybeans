package com.buzybeans.core.beans;

import com.buzybeans.core.api.Application;
import com.buzybeans.core.classloading.BuzybeansClassloader;
import com.buzybeans.core.jpa.JPAService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.ejb.Singleton;
import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.persistence.Entity;

public class EJBApplication implements Application {

    private BuzybeansClassloader classLoader;

    private List<Bean<?>> beans;

    private Collection<Class<?>> entities;

    private JPAService jpaService;

    private ApplicationArchive archive;

    private String containerID;

    public EJBApplication(ApplicationArchive archive) {
        this.classLoader = new BuzybeansClassloader(this, getClass().getClassLoader());
        this.beans = new ArrayList<Bean<?>>();
        this.entities = new ArrayList<Class<?>>();
        this.archive = archive;
        this.archive.init(classLoader);
        this.jpaService = new JPAService(classLoader);
        this.containerID = UUID.randomUUID().toString();
    }

    @Override
    public String getContainerID() {
        return containerID;
    }

    @Override
    public void start() {
        initManageable();
        jpaService.setEntities(entities);
        jpaService.start();
        for (Bean<?> bean : beans) {
            if (bean.isStartable()) {
                bean.start();
            }
        }
    }

    @Override
    public void stop() {
        for (Bean<?> bean : beans) {
            bean.stop();
        }
        jpaService.stop();
    }

    @Override
    public <T> T getInstance(Class<T> clazz) {
        return null;
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

    public ApplicationArchive getArchive() {
        return archive;
    }

    public Collection<Class<?>> getApplicationClasses() {
        return Collections.unmodifiableCollection(archive.getClasses());
    }

    private void initManageable() {
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
