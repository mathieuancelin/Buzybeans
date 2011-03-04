package com.buzybeans.core.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 * @author mathieuancelin
 */
public class ClassUtils {

    public static String filenameToClassname(String filename) {
        return filename.substring(0, filename.lastIndexOf(".class")).replace('/', '.').replace('\\', '.');
    }

    public static Map<String, FromReader> loadFromJar(File jar) throws Exception {
        Map<String, FromReader> classPath = new HashMap<String, FromReader>();
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
                    // TODO : read from embbeded jars
                } else {
                    System.out.println("found : " + entry.getName());
                }
            }
        }
        return classPath;
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

    public static interface FromReader {

        byte[] read() throws IOException ;
    }

    public static class FromFileReader implements FromReader {

        private final File file;

        public FromFileReader(File file) {
            this.file = file;
        }

        @Override
        public byte[] read() throws IOException {
            return getClassDefinition(file);
        }
    }

    public static class FromJarReader implements FromReader {

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
