package com.buzybeans.core.deploy;

import com.buzybeans.core.util.ZipUtil;
import com.buzybeans.core.BuzyBeans;
import com.buzybeans.core.beans.EJBApplication;
import com.buzybeans.core.conf.BuzybeansConfig;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DeployMonitor extends Thread {

    private class WarFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            if (name.endsWith(".war")) {
                if (new File(dir, name).isDirectory()) {
                    return false;
                }
                return true;
            }
            return false;
        }
    }

    private final BuzyBeans buzybeans;
    
    private Map<String, Long> deployedFiles =
            new HashMap<String, Long>();

    public DeployMonitor(final BuzyBeans buzybeans) {
        this.buzybeans = buzybeans;
    }

    @Override
    public void run() {
        WarFilter filter = new WarFilter();
        File work = BuzybeansConfig.workDir;
        File deploy = BuzybeansConfig.deployDir;
        for (File file : deploy.listFiles(filter)) {
            try {
                deploy(file, work);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        while(true) {
            List<String> present = new ArrayList<String>();
            for (File file : deploy.listFiles(filter)) {
                present.add(file.getAbsolutePath());
                if (deployedFiles.containsKey(file.getAbsolutePath())) {
                    if (file.lastModified() > deployedFiles.get(file.getAbsolutePath())) {
                        try {
                            undeploy(file.getAbsolutePath(), work);
                            deploy(file, work);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    try {
                        deploy(file, work);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            Set<String> keySet = deployedFiles.keySet();
            if (present.size() != keySet.size()) {
                for (String war : keySet) {
                    if (!present.contains(war)) {
                        try {
                            undeploy(war, work);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            present.clear();
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void deploy(File file, File work) {
        long start = System.currentTimeMillis();
        File deployed = ZipUtil.inflate(file, work);
        List<File> workFiles = getDeployedFiles(deployed, new ArrayList<File>());
        EJBApplication application = new EJBApplication();
        application.getClassLoader().setFiles(workFiles);
        application.getClassLoader().setBase(deployed);
        application.getClassLoader().loadDeployedClasses();
        application.initManageable();
        application.start();
        buzybeans.getApplications().put(deployed.getAbsolutePath(), application);
        deployedFiles.put(file.getAbsolutePath(), file.lastModified());
        System.out.println("Application deployed in : " + (System.currentTimeMillis() - start) + " ms");
    }

    private void undeploy(String war, File work) {
        buzybeans.getApplications().get(new File(work, new File(war).getName()).getAbsolutePath()).stop();
        rm (new File(work, new File(war).getName()));
        deployedFiles.remove(war);
    }

    private void rm(File dir) {
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                rm(file);
            }
            dir.delete();
        } else {
            dir.delete();
        }
    }

    private List<File> getDeployedFiles(File dir, List<File> deployedFiles) {
        if (dir.isDirectory()) {
            for(File file : dir.listFiles()) {
                getDeployedFiles(file, deployedFiles);
            }
        } else {
            deployedFiles.add(dir);
        }
        return deployedFiles;
    }
}
