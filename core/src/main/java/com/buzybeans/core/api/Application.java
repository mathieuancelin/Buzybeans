package com.buzybeans.core.api;

/**
 *
 * @author mathieuancelin
 */
public interface Application {

    String getContainerID();

    <T> T getInstance(Class<T> clazz);

    void start();

    void stop();

}
