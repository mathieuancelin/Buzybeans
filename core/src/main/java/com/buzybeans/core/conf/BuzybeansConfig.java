package com.buzybeans.core.conf;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.core.ConsoleAppender;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mathieu
 */
public class BuzybeansConfig {

    public static final Logger logger = LoggerFactory.getLogger(BuzybeansConfig.class);
    private static final Logger hibernate = LoggerFactory.getLogger("org.hibernate");
    public final static String WORK_DIR = "work";
    public final static String DEPLOY_DIR = "deploy";

    public static File workDir;
    public static File deployDir;

    public static void init() {
        workDir = new File(WORK_DIR);
        if (!workDir.exists()) {
            workDir.mkdirs();
        }
        deployDir = new File(DEPLOY_DIR);
        if (!deployDir.exists()) {
            deployDir.mkdirs();
        }
        initLogger();
    }

    private static void initLogger() {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        lc.stop(); lc.reset();
        ch.qos.logback.classic.Logger backLogger = (ch.qos.logback.classic.Logger) logger;
        ConsoleAppender consoleAppender = new ConsoleAppender();
        consoleAppender.setContext(lc);
        PatternLayout pl = new PatternLayout();
        pl.setPattern("%d{HH:mm:ss.SSS} %-5level - %msg%n");
        pl.setContext(lc);
        pl.start();
        consoleAppender.setLayout(pl);
        consoleAppender.start();
        backLogger.detachAndStopAllAppenders();
        backLogger.addAppender(consoleAppender);
        backLogger.setLevel(Level.TRACE);
        ((ch.qos.logback.classic.Logger) hibernate).setLevel(Level.OFF);
        ((ch.qos.logback.classic.Logger) hibernate).addAppender(consoleAppender);
        lc.start();
    }
}
