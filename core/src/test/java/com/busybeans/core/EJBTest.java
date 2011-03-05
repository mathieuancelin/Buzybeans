package com.busybeans.core;

import com.buzybeans.core.BuzyBeans;
import com.buzybeans.core.api.Application;
import com.buzybeans.core.api.Archive;
import com.buzybeans.core.conf.BuzybeansConfig;
import java.io.File;
import org.junit.Assert;
import org.junit.Test;

public class EJBTest {

    public static int actions;

    @Test
    public void testApp() {
        actions = 0;
        BuzybeansConfig.initLogger();
        Archive archive = BuzyBeans.newArchive();
        archive
                .addClass(SingletonBean.class)
                .addClass(Person.class)
                .addFile(new File("src/test/resources/persistence.xml")
                         , "META-INF/persistence.xml");
        Application application = BuzyBeans.newApplication(archive);
        application.start();
        Assert.assertNotNull(application.getResource("META-INF/persistence.xml").toString());
        application.stop();
        Assert.assertTrue(actions == 2);
    }
}
