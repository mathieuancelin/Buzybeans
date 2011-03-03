package com.buzybeans.core;

import com.buzybeans.core.beans.EJBApplication;
import com.buzybeans.core.jpa.JPAService;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author mathieu
 */
public class BuzyBeans {

    private boolean started = false;

    public Map<String, EJBApplication> applications =
            new HashMap<String, EJBApplication>();

    public void start() {
        started = true;
    }

    public void stop() {
        for (EJBApplication application : applications.values()) {
            application.stop();
        }
        started = false;
    }

    boolean isStopped() {
        return !started;
    }

    public Map<String, EJBApplication> getApplications() {
        return applications;
    }
}
