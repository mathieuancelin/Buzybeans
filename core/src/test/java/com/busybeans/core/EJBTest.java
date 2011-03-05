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
        // init loggers to avoid hibernate debug info
        BuzybeansConfig.initLogger();

        // building the virtual application archive
        Archive archive = BuzyBeans.newArchive();
        archive
                .addClass(SingletonBeanA.class)
                .addClass(SingletonBeanB.class)
                .addClass(Person.class)
                .addFile(new File("src/test/resources/persistence.xml")
                         , "META-INF/persistence.xml");

        // Building the EJB container based on the virtual archive
        Application application = BuzyBeans.newApplication(archive);
        // Starting the EJB container
        application.start();

        // check that the persistence file is present
        Assert.assertNotNull(
                application.getResource("META-INF/persistence.xml").toString());

        // lookup an EJB
        SingletonBeanA singletonA = application.getInstance(SingletonBeanA.class);

        // check for injection
        Assert.assertEquals(SingletonBeanB.SOMETHING, singletonA.doSomething());
        Assert.assertNotNull(singletonA.getEm());

        // stop the container
        application.stop();

        // check for postconstruct and predestroy
        Assert.assertTrue(actions == 2);
    }
}
