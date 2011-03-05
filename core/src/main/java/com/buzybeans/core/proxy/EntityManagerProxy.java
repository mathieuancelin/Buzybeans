package com.buzybeans.core.proxy;

import com.buzybeans.core.jpa.JPAService;
import java.lang.reflect.Method;
import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import javax.persistence.EntityManager;

/**
 *
 * @author mathieuancelin
 */
public class EntityManagerProxy implements MethodFilter, MethodHandler {

    private static EntityManager em;

    @Override
    public Object invoke(Object o, Method method, Method proceed, Object[] os) throws Throwable {
        return method.invoke(JPAService.currentEm.get(), os);
    }

    @Override
    public boolean isHandled(Method method) {
        return true;
    }

    public static  EntityManager getEmProxy() {
        if (em == null) {
            ProxyFactory fact = new ProxyFactory();
            EntityManagerProxy handler = new EntityManagerProxy();
            fact.setInterfaces(new Class[] {EntityManager.class});
            fact.setFilter(handler);
            Class newBeanClass = fact.createClass();
            EntityManager scopedObject = null;
            try {
                scopedObject = (EntityManager) newBeanClass.cast(newBeanClass.newInstance());
            } catch (Exception ex) {
                throw new IllegalStateException("Unable to create proxy for object EntityManager", ex);
            }
            ((ProxyObject) scopedObject).setHandler(handler);
            em = scopedObject;
        }
        return em;
    }
}
