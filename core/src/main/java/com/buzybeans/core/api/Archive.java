package com.buzybeans.core.api;

import com.buzybeans.core.beans.ApplicationArchive;
import java.io.File;

/**
 *
 * @author mathieuancelin
 */
public interface Archive {

    ApplicationArchive addClass(Class<?> clazz);

    ApplicationArchive addClasses(Class<?>... addclasses);

    ApplicationArchive addFile(File file);

    ApplicationArchive addFile(File file, String name);

    ApplicationArchive addFile(File... files);

}
