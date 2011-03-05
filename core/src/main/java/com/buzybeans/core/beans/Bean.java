package com.buzybeans.core.beans;

import com.buzybeans.core.injection.ClassHandler;
import com.buzybeans.core.jpa.JPAService;
import com.buzybeans.core.proxy.DynamicProxy;
import com.buzybeans.core.proxy.ProxyHelper;
import com.buzybeans.core.util.Pool;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Startup;
import javax.interceptor.Interceptors;

/**
 *
 * @author mathieu
 */
public class Bean<T> {

    public enum Type {
        SINGLETON, STATELESS, STATEFUL
    }

    private T singletonInstance;

    private ConcurrentHashMap<String, T> statefulInstances = new ConcurrentHashMap<String, T>();

    private Pool<T> statelessInstances;

    private final Type type;

    private final Class<T> clazz;

    private List<Method> methods;

    private EJBApplication app;

    private DynamicProxy proxy;

    private T proxyfiedInstance;

    private ReadWriteLock lock;

    private ClassHandler classHandler;

    public static <R> Bean<R> newBean(Type type, Class<R> clazz, EJBApplication app) {
        return new Bean<R>(type, clazz, app);
    }
    
    public Bean(Type type, Class<T> clazz, EJBApplication app) {
        this.type = type;
        this.clazz = clazz;
        this.app = app;
        methods = new ArrayList<Method>();
        methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
        for (Method m : clazz.getMethods()) {
            if (!methods.contains(m)) {
                methods.add(m);
            }
        }
        proxy = DynamicProxy.getDynamicProxy(clazz, this);
        proxyfiedInstance = (T) ProxyHelper.createProxy(proxy);
        lock = new ReentrantReadWriteLock();
        classHandler = new ClassHandler();
    }

    public Type getType() {
        return type;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public void start() {
        try {
            for (Method m : methods) {
                if (m.isAnnotationPresent(PostConstruct.class)
                        && m.getParameterTypes().length == 0) {
                    getInstance(false); // TODO : fixe ugly workaround
                    m.invoke(getProxiedInstance());
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void stop() {
        try {
            for (Method m : methods) {
                if (m.isAnnotationPresent(PreDestroy.class)
                        && m.getParameterTypes().length == 0) {
                    m.invoke(getProxiedInstance());
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public T getProxiedInstance() {
        return proxyfiedInstance;
    }

    public T getInstance() {
        return getInstance(true);
    }

    private T getInstance(boolean start) {
        if (singletonInstance == null) {
            try {
                singletonInstance = clazz.newInstance();
                classHandler.classInjection(singletonInstance, clazz, new ArrayList<Method>(), app);
                if (start) {
                    start();
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        return singletonInstance;
    }

    public void ungetInstance(T instance) {
        // nothing 
    }

    public Object localCall(Method method, Object[] args) throws Throwable {
        T instance = getInstance();
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(app.getClassLoader());
        Lock writeLock = this.lock.writeLock();
        writeLock.lock();
        //app.getJpaService().startTx();
        try {
            Object ret = method.invoke(instance, args);
            //app.getJpaService().stopTx(false);
            return ret;
        } catch (Exception e) {
            //app.getJpaService().stopTx(true);
            throw e;
        } finally {
            ungetInstance(instance);
            writeLock.unlock();
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    public boolean isStartable() {
        return clazz.isAnnotationPresent(Startup.class);
    }

    public boolean isIntercepted() {
        return clazz.isAnnotationPresent(Interceptors.class);
    }
}
