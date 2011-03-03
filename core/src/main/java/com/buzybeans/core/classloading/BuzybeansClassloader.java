package com.buzybeans.core.classloading;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 * @author mathieu
 */
public class BuzybeansClassloader extends ClassLoader {

    private File base;
    private List<File> files;
    private Map<String, FromReader> classPath = new HashMap<String, FromReader>();
    private List<Class<?>> classes = new ArrayList<Class<?>>();

    public BuzybeansClassloader(ClassLoader parent) {
        super(parent);
    }

    @Override
    public URL getResource(String name) {
        try {
            return new File(base, name).toURI().toURL();
        } catch (MalformedURLException ex) {
            return null;
        }
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        try {
            return new FileInputStream(new File(base, name));
        } catch (FileNotFoundException ex) {
            return null;
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
        if (classPath.containsKey(name)) {
            FromReader reader = classPath.get(name);
            try {
                byte[] bytes = reader.read();
                return defineClass(name, bytes, 0, bytes.length);
            } catch (IOException ex) {
                throw new ClassNotFoundException("classe non trouvée : ", ex);
            }
        } else {
            //System.out.println("non trouvée : " + name);
        }
        return super.loadClass(name);
    }

    public void loadDeployedClasses() {
        if (classPath.isEmpty()) {
            listDeployedClasses();
        }
        for (String name : classPath.keySet()) {
            try {
                Class<?> clazz = loadClass(name);
                classes.add(clazz);
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void listDeployedClasses() {
        for (File file : files) {
            if (file.getName().endsWith(".class")) {
                String name = filenameToClassname(
                        file.getAbsolutePath().replace(base.getAbsolutePath(), "").replace("/WEB-INF/classes/", ""));
                classPath.put(name, new FromFileReader(file));
            }
            if (file.getName().endsWith(".jar")) {
                try {
                    loadFromJar(file);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public List<Class<?>> getClasses() {
        return classes;
    }

    public void setBase(File base) {
        this.base = base;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public void loadFromJar(File jar) throws Exception {
        ZipFile zip = new ZipFile(jar);
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (!entry.isDirectory()) {
                if (entry.getName().endsWith(".class")) {
                    try {
                        String name = filenameToClassname(entry.getName());
                        classPath.put(name, new FromJarReader(jar, name));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else if (entry.getName().endsWith(".jar")) {
                    System.out.println("jar : " + entry.getName());
                } else {
                }
            }
        }
    }

    public static byte[] getClassDefinition(File file) {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        if (is == null) {
            return null;
        }
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int count;
            while ((count = is.read(buffer, 0, buffer.length)) > 0) {
                os.write(buffer, 0, count);
            }
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static String filenameToClassname(String filename) {
        return filename.substring(0, filename.lastIndexOf(".class")).replace('/', '.').replace('\\', '.');
    }

    private interface FromReader {

        byte[] read() throws IOException ;
    }

    private class FromFileReader implements FromReader {

        private final File file;

        public FromFileReader(File file) {
            this.file = file;
        }

        @Override
        public byte[] read() throws IOException {
            return getClassDefinition(file);
        }
    }

    private class FromJarReader implements FromReader {

        private final File file;
        private final String name;

        public FromJarReader(File file, String name) {
            this.file = file;
            this.name = name;
        }

        @Override
        public byte[] read() throws IOException {
            ZipFile zip = new ZipFile(file);
            ZipEntry entry = zip.getEntry(name.replace(".", "/") + ".class");
            if (entry != null) {
                byte[] array = new byte[1024];
                InputStream in = zip.getInputStream(entry);
                ByteArrayOutputStream out = new ByteArrayOutputStream(array.length);
                int length = in.read(array);
                while (length > 0) {
                    out.write(array, 0, length);
                    length = in.read(array);
                }
                return out.toByteArray();
            }
            throw new RuntimeException("impossible de lire la classe " + name + " depuis " + file.getAbsolutePath());
        }
    }
}
