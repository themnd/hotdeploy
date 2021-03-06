package com.polopoly.ps.hotdeploy.discovery;

import java.io.File;
import java.util.List;

import com.polopoly.ps.hotdeploy.discovery.NotApplicableException;
import com.polopoly.ps.hotdeploy.discovery.importorder.ImportOrderFileDiscoverer;
import com.polopoly.ps.hotdeploy.file.DeploymentFile;
import com.polopoly.ps.hotdeploy.file.FileDeploymentFile;

import junit.framework.TestCase;

public class TestFileImportOrderFileDiscoverer extends TestCase {
    public void testDiscoverer() throws NotApplicableException {
        String directory = "src" + File.separator + "test" + File.separator + "resources";

        List<DeploymentFile> files = new ImportOrderFileDiscoverer(new File(directory)).getFilesToImport();

        String subFolder = directory + File.separator + "folder" + File.separator + "subfolder";

        assertEquals(new FileDeploymentFile(new File(directory, "a.xml")), files.get(0));
        assertEquals(new FileDeploymentFile(new File(subFolder, "c.xml")), files.get(1));
        assertEquals(new FileDeploymentFile(new File(subFolder, "d.xml")), files.get(2));

        assertEquals(3, files.size());
    }
}
