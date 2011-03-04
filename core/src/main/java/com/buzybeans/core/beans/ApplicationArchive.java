package com.buzybeans.core.beans;

import com.buzybeans.core.util.ClassUtils;
import com.buzybeans.core.util.ClassUtils.FromFileReader;
import com.buzybeans.core.util.ClassUtils.FromReader;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author mathieuancelin
 */
public class ApplicationArchive {

    private File deployBase;
    private List<File> deployedFiles = new ArrayList<File>();
    private Map<String, FromReader> classPath = new HashMap<String, FromReader>();
    private Map<String, Class<?>> classes = new HashMap<String, Class<?>>();
    private Map<String, File> resourceFiles = new HashMap<String, File>();

    public ApplicationArchive addClass(Class<?> clazz) {
        classes.put(clazz.getName(), clazz);
        return this;
    }

    public ApplicationArchive addClasses(Class<?>... addclasses) {
        for (Class<?> clazz : addclasses) {
            classes.put(clazz.getName(), clazz);
        }
        return this;
    }

    public ApplicationArchive addFile(File file) {
        resourceFiles.put(file.getName(), file);
        return this;
    }

    public ApplicationArchive addFile(File... files) {
        for (File file : files) {
            resourceFiles.put(file.getName(), file);
        }
        return this;
    }

    public ApplicationArchive addFile(File file, String name) {
        resourceFiles.put(name, file);
        return this;
    }

    public Collection<Class<?>> getClasses() {
        return classes.values();
    }

    public File getResource(String name) {
        return resourceFiles.get(name);
    }

    public Class<?> getClass(String name) {
        return classes.get(name);
    }

    void init(ClassLoader loader) {
        listDeployedClasses();
        loadDeployedClasses(loader);
    }

    public void setDeployedFiles(List<File> files) {
        this.deployedFiles = files;
    }

    public void setDeployBase(File base) {
        this.deployBase = base;
    }

    public Map<String, FromReader> getClassPath() {
        return classPath;
    }

    private void listDeployedClasses() {
        for (File file : deployedFiles) {
            if (file.getName().endsWith(".class")) {
                String name = ClassUtils.filenameToClassname(
                        file.getAbsolutePath().replace(deployBase.getAbsolutePath(), "").replace("/WEB-INF/classes/", ""));
                classPath.put(name, new FromFileReader(file));
            }
            if (file.getName().endsWith(".jar")) {
                try {
                    classPath.putAll(ClassUtils.loadFromJar(file));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void loadDeployedClasses(ClassLoader loader) {
        for (String name : classPath.keySet()) {
            try {
                Class<?> clazz = loader.loadClass(name);
                addClass(clazz);
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }
    }
}
