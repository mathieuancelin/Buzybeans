package com.buzybeans.core;

import com.buzybeans.core.beans.EJBApplication;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author mathieu
 */
public class BuzyBeans {

    private boolean started = false;

    private Map<String, EJBApplication> applications =
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

    public void addApplication(EJBApplication application) {
        applications.put(application.getContainerID(), application);
    }

    public EJBApplication getApplication(String id) {
        return applications.get(id);
    }
}
