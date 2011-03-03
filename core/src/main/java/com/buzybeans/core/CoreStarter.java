package com.buzybeans.core;

import com.buzybeans.core.conf.BuzybeansConfig;
import com.buzybeans.core.deploy.DeployMonitor;

public class CoreStarter {

    private static final long SLEEP_TIME = 1000L;

    public static void main(String[] args) {
        System.out.println("+==========================================+");
        System.out.println("|                                          |");
        System.out.println("|           Welcome in BuzyBeans !         |");
        System.out.println("|                                          |");
        System.out.println("+==========================================+\n");
        
        BuzybeansConfig.init();
        BuzyBeans buzybeans = new BuzyBeans();
        Runtime.getRuntime().addShutdownHook(new ShutdownHook(buzybeans));
        new ShutdownMonitorThread(buzybeans).start();
        buzybeans.start();
        DeployMonitor monitor = new DeployMonitor(buzybeans);
        monitor.start();
    }

    static class ShutdownMonitorThread extends Thread {

        private BuzyBeans embedded = null;

        public ShutdownMonitorThread(final BuzyBeans embedded) {
            this.embedded = embedded;
        }

        @Override
        public void run() {
            while (!this.embedded.isStopped()) {
                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {

                }
            }
            System.exit(0);
        }
    }

    static class ShutdownHook extends Thread {

        private BuzyBeans embedded = null;

        public ShutdownHook(final BuzyBeans embedded) {
            this.embedded = embedded;
        }

        @Override
        public void run() {
            try {
                if (!this.embedded.isStopped()) {
                    this.embedded.stop();
                }
            } catch (Exception e) {
                System.err.println("Error while stopping embedded server");
                e.printStackTrace(System.err);
            }
        }
    }
}
