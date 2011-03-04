package com.buzybeans.core.classloading;

import com.buzybeans.core.beans.EJBApplication;
import com.buzybeans.core.util.ClassUtils.FromReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

/**
 *
 * @author mathieu
 */
public class BuzybeansClassloader extends ClassLoader {

    private EJBApplication app;

    public BuzybeansClassloader(EJBApplication app, ClassLoader parent) {
        super(parent);
        this.app = app;
    }

    @Override
    public URL getResource(String name) {
        try {
            return app.getArchive().getResource(name).toURI().toURL();
        } catch (Exception ex) {
            return getParent().getResource(name);
        }
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        try {
            return new FileInputStream(app.getArchive().getResource(name));
        } catch (Exception ex) {
            return getParent().getResourceAsStream(name);
        }
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return super.getResources(name);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> c = findLoadedClass(name);
        if (c != null) {
            return c;
        }
        if (app.getArchive().getClassPath().containsKey(name)) {
            FromReader reader = app.getArchive().getClassPath().get(name);
            try {
                byte[] bytes = reader.read();
                return defineClass(name, bytes, 0, bytes.length);
            } catch (IOException ex) {
                throw new ClassNotFoundException("classe non trouvee : ", ex);
            }
        } else {
//            c = app.getClass(name);
//            if (c != null) {
//                return c;
//            }
        }
        return super.loadClass(name);
    }
}
