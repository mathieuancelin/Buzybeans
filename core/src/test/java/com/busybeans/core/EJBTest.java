package com.busybeans.core;

import com.buzybeans.core.beans.ApplicationArchive;
import com.buzybeans.core.beans.EJBApplication;
import java.io.File;
import org.junit.Assert;
import org.junit.Test;

public class EJBTest {

    public static int actions;

    @Test
    public void testApp() {
        actions = 0;
        ApplicationArchive archive = new ApplicationArchive();
        archive
                .addClass(SingletonBean.class)
                .addClass(Person.class)
                .addFile(new File("src/test/resources/persistence.xml")
                         , "META-INF/peristence.xml");
        EJBApplication application = new EJBApplication(archive);
        application.start();
        application.stop();
        Assert.assertTrue(actions == 2);
    }
}
