/*
 *  Copyright 2010 mathieuancelin.
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

package com.buzybeans.core.proxy;

import com.buzybeans.core.beans.Bean;
import java.lang.reflect.Method;
import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;

/**
 *
 * @author Mathieu ANCELIN
 */
public class DynamicProxy<T> implements MethodFilter, MethodHandler {

    private final Bean<T> bean;

    private final Class<T> type;

    public static <R> DynamicProxy<R> getDynamicProxy(Class<R> type, Bean<R> bean) {
        return new DynamicProxy<R>(type, bean);
    }

    private DynamicProxy(Class<T> type, Bean<T> bean) {
        this.bean = bean;
        this.type = type;
    }

    public Class<T> getType() {
        return type;
    }

    @Override
    public boolean isHandled(Method method) {
        return true;
    }

    @Override
    public Object invoke(Object self, Method method, Method proceed, Object[] args) throws Throwable {
        return bean.localCall(method, args);
    }
}
