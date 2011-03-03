/*
 *  Copyright 2011 mathieuancelin.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package com.buzybeans.core.jpa;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.sql.DataSource;
import org.hibernate.ejb.Ejb3Configuration;

/**
 *
 * @author mathieuancelin
 */
public class JPAService {

    private static JPAService INSTANCE;

    private boolean started = false;

    private DataSource dataSource;

    private EntityManagerFactory emf;

    public static ThreadLocal<EntityManager> currentEm =
            new ThreadLocal<EntityManager>() {
        @Override
        protected EntityManager initialValue() {
            return null;
        }
    };

    private Collection<Class<?>> entities;

    private final ClassLoader loader;

    public JPAService(ClassLoader loader) {
        this.loader = loader;
    }

    public void setEntities(Collection<Class<?>> entities) {
        this.entities = entities;
    }

    public synchronized void start() {
        try {
            launchJPA();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public synchronized void stop() {
        if (started) {
            if (emf != null)
                emf.close();
            if (dataSource instanceof ComboPooledDataSource) {
                try {
                ((ComboPooledDataSource) dataSource).close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            started = false;
        }
    }

    public void startTx() {
        if (started) {
            EntityManager manager = emf.createEntityManager();
            manager.setFlushMode(FlushModeType.COMMIT);
            manager.getTransaction().begin();
            JPAService.currentEm.set(manager);
        }
    }

    public void stopTx(boolean rollback) {
        if (started) {
            EntityManager manager = JPAService.currentEm.get();
            try {
                if (rollback) {
                    manager.getTransaction().rollback();
                } else {
                    manager.getTransaction().commit();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            JPAService.currentEm.remove();
        }
    }

    private void launchJPA() throws Exception {
        System.setProperty("com.mchange.v2.log.MLog", "com.mchange.v2.log.FallbackMLog");
        System.setProperty("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "OFF");
        ComboPooledDataSource intDataSource = new ComboPooledDataSource();
        intDataSource.setDriverClass("org.hsqldb.jdbcDriver");
        //intDataSource.setJdbcUrl("jdbc:hsqldb:hsql://localhost/testdb");
        intDataSource.setJdbcUrl("jdbc:hsqldb:file:" + (new File("webframeworkdb").getAbsolutePath()));
        intDataSource.setUser("sa");
        intDataSource.setPassword("");
        intDataSource.setAcquireRetryAttempts(10);
        intDataSource.setCheckoutTimeout(5000);
        intDataSource.setBreakAfterAcquireFailure(false);
        intDataSource.setMaxPoolSize(30);
        intDataSource.setMinPoolSize(1);
        intDataSource.setIdleConnectionTestPeriod(10);
        intDataSource.setTestConnectionOnCheckin(true);
        dataSource = intDataSource;
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(loader);
        Ejb3Configuration cfg = new Ejb3Configuration();
        cfg.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        cfg.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
        cfg.setProperty("javax.persistence.transactionType", "RESOURCE_LOCAL");
        for(Class<?> entity : entities) {
            cfg.addAnnotatedClass(entity);
        }
        cfg.setDataSource(dataSource);
        this.emf = cfg.buildEntityManagerFactory();
        Thread.currentThread().setContextClassLoader(oldClassLoader);
        started = true;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
}
