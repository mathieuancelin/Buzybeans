package com.buzybeans.core.api;

import java.net.URL;
import java.util.Collection;

/**
 *
 * @author mathieuancelin
 */
public interface Application {

    String getContainerID();

    <T> T getInstance(Class<T> clazz);

    void start();

    void stop();

    URL getResource(String name);

    Collection<URL> getResources(String name);

}
