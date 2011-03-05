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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import javax.ejb.EJB;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class that handle injection on the fields of a class.
 *
 * @author Mathieu ANCELIN
 */
public final class FieldsHandler {

    private static final Logger logger = LoggerFactory.getLogger(ClassHandler.class);

    /**
     * Constructor.
     */
    public FieldsHandler() {
    }

    /**
     * Inject every fields of an object.
     * 
     * @param <T> the type of the injected object.
     * @param instance the injected object.
     * @param c the class of the object.
     * @param staticInjection can we inject static fields.
     * @param injector the concerned injector.
     * @throws IllegalAccessException
     */
    public <T> void fieldsInjection(T instance, Class<?> c,
            EJBApplication injector) throws IllegalAccessException {
        Field[] fieldsOfTheClass = c.getDeclaredFields();
        // for each declared fields
        for (Field field : fieldsOfTheClass) {
            EJB annotation = field.getAnnotation(EJB.class);
            PersistenceContext em = field.getAnnotation(PersistenceContext.class);
            // check if field is injectable and if you can inject static fields
            if (annotation != null || em != null) {
                // check if the field is not final
                if (Modifier.isFinal(field.getModifiers())) {
                    throw new RuntimeException("Cannot inject final field: " + field);
                }
                Class<?> type = null;
                if (annotation != null) {
                    type = field.getType();
                } else {
                    type = EntityManager.class;
                }
                // get an instance of the field (simple instance or provided one
                Object injectedObject = null;
                if (annotation != null) {
                    injectedObject = injector.getBean(type).getProxiedInstance();
                } else {
                    injectedObject = EntityManagerProxy.getEmProxy();
                }
                boolean accessible = field.isAccessible();
                // if field is private then put it private for injectedObject setting
                if (!accessible) {
                    field.setAccessible(true);
                }
                try {
                    field.set(instance, injectedObject);
                } finally {
                    // if the field was private, then put it private back
                    if (!accessible) {
                        field.setAccessible(accessible);
                    }
                }
            }
        }
    }
}
