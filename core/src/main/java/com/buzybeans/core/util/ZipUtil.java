package com.buzybeans.core.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 *
 * @author mathieu
 */
public class ZipUtil {

    private static final int BUFFER = 2048;

    public static File inflate(File war, File destination) {
        try {
            BufferedOutputStream dest = null;
            BufferedInputStream is = null;
            ZipEntry entry;
            ZipFile zipfile = new ZipFile(war);
            Enumeration e = zipfile.entries();
            while (e.hasMoreElements()) {
                entry = (ZipEntry) e.nextElement();
                if (entry.isDirectory()) {
                    File dir = new File(new File(destination.getAbsoluteFile(), war.getName()), entry.getName());
                    dir.mkdirs();
                } else {
                    is = new BufferedInputStream(zipfile.getInputStream(entry));
                    int count;
                    byte data[] = new byte[BUFFER];
                    FileOutputStream fos = new FileOutputStream(
                            new File(new File(destination.getAbsoluteFile(), war.getName()), entry.getName()));
                    dest = new BufferedOutputStream(fos, BUFFER);
                    while ((count = is.read(data, 0, BUFFER))
                            != -1) {
                        dest.write(data, 0, count);
                    }
                    dest.flush();
                    dest.close();
                    is.close();
                }
            }
            return new File(destination.getAbsoluteFile(), war.getName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
