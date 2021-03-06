/*
 *  Copyright 2009-2010 Mathieu ANCELIN.
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
package com.buzybeans.core.injection;

import com.buzybeans.core.beans.EJBApplication;
import com.buzybeans.core.proxy.EntityManagerProxy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import javax.ejb.EJB;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class that handle injection on the methods of a class.
 *
 * @author Mathieu ANCELIN
 */
public final class MethodHandler {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandler.class);

    /**
     * Constructor.
     */
    public MethodHandler() {
    }

    /**
     * Inject all injectable methods of an object.
     * 
     * @param <T> type of the injected object.
     * @param instance instance of the injected object.
     * @param c class of the injected object.
     * @param maybeOverrides list of potential overridden methods.
     * @param staticInjection can we inject static fields.
     * @param injector the concerned injector.
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public <T> void methodsInjection(T instance, Class<?> c,
            List<Method> maybeOverrides,
            EJBApplication injector) throws IllegalAccessException,
            InvocationTargetException {

        Method[] methodsOfTheClass = c.getDeclaredMethods();
        // for each method of the class
        for (Method method : methodsOfTheClass) {
            EJB annotation = method.getAnnotation(EJB.class);
            PersistenceContext em = method.getAnnotation(PersistenceContext.class);
            // check if method is injectable and if you can inject static methods and if method is overriden
            if ((annotation != null || em != null)
                    && !isOverridden(method, maybeOverrides)) {
                Class<?>[] parameterTypes = parameterTypes = method.getParameterTypes();
                Object[] parameters = new Object[parameterTypes.length];
                for (int j = 0; j < parameterTypes.length; j++) {
                    // inject parameters instance (simple instances or provided)
                    if (em != null && parameterTypes[j].equals(EntityManager.class)) {
                        parameters[j] = EntityManagerProxy.getEmProxy();
                    } else {
                        parameters[j] = injector.getBean(parameterTypes[j]).getProxiedInstance();
                    }
                }
                boolean accessible = method.isAccessible();
                // set a private method as public method to invoke it
                if (!accessible) {
                    method.setAccessible(true);
                }
                // invocation of the method with rights parameters
                try {
                    method.invoke(instance, parameters);
                } finally {
                    // if method was private, then put it private back
                    if (!accessible) {
                        method.setAccessible(accessible);
                    }
                }
            }
        }
    }

    /**
     * Check if a method is overridden by another one contained in a list.
     *
     * @param method method to check.
     * @param maybeOverrides methods that can overrides method.
     * @return if the method is overridden.
     */
    private static boolean isOverridden(Method method, List<Method> maybeOverrides) {
        for (Method candidate : maybeOverrides) {
            if (isOverridden(method, candidate)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a method is overriden by another one.
     *
     * @param method method to check.
     * @param candidate method to check against.
     * @return if the method is overridden.
     */
    private static boolean isOverridden(Method method, Method candidate) {
        // check f names are the same
        if (!method.getName().equals(candidate.getName())) {
            return false;
        }
        int modifiers = candidate.getModifiers();
        // check if candidate is private
        boolean isPrivate = Modifier.isPrivate(modifiers);
        if (isPrivate) {
            return false;
        }
        // check if candidate is static
        boolean isStatic = Modifier.isStatic(modifiers);
        if (isStatic) {
            return false;
        }
        boolean isDefault = !isPrivate
                && !Modifier.isPublic(modifiers)
                && !Modifier.isProtected(modifiers);
        boolean samePackage =
                method.getDeclaringClass().getPackage()
                == candidate.getDeclaringClass().getPackage();
        if (isDefault && !samePackage) {
            return false;
        }
        // check if parameters are the same
        Class<?>[] methodParameters = method.getParameterTypes();
        Class<?>[] candidateParameters = candidate.getParameterTypes();
        // check numbers of parameters
        if (methodParameters.length != candidateParameters.length) {
            return false;
        }
        // check types of parameters
        for (int i = 0; i < methodParameters.length; i++) {
            Class<?> class1 = methodParameters[i];
            Class<?> class2 = candidateParameters[i];
            if (!class1.equals(class2)) {
                return false;
            }
        }
        return true;
    }
}
