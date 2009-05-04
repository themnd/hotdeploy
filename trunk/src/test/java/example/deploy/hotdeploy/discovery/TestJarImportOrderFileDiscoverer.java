package example.deploy.hotdeploy.discovery;

import static example.deploy.hotdeploy.discovery.TestFileConstants.DEPENDED_TEST_JAR_FILE_NAME;
import static example.deploy.hotdeploy.discovery.TestFileConstants.DEPENDING_TEST_JAR_PATH;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

import junit.framework.TestCase;
import example.deploy.hotdeploy.file.DeploymentDirectory;
import example.deploy.hotdeploy.file.JarDeploymentFile;
import example.deploy.hotdeploy.file.JarDeploymentRoot;

public class TestJarImportOrderFileDiscoverer extends TestCase {
    public void testDiscoverer() throws NotApplicableException, IOException {
        String fileName = PlatformNeutralPath.unixToPlatformSpecificPath("src/test/resources/test.jar");

        JarFile file = new JarFile(new File(fileName));
        DeploymentDirectory dir = new JarDeploymentRoot(file);

        ImportOrderFile files = new ImportOrderFileDiscoverer().getFilesToImport(dir);

        assertEquals(new JarDeploymentFile(file, file.getEntry("b/c.xml")), files.get(0));

        assertEquals("test", files.calculateDependencyName());
        assertEquals(1, files.size());
        assertEquals(0, files.getDependencies().size());
    }

    public void testDiscoverResources() throws Exception {
        String fileName = DEPENDING_TEST_JAR_PATH;
        String dependedFileNameWithVersion = DEPENDED_TEST_JAR_FILE_NAME;

        int i = dependedFileNameWithVersion.indexOf('-');

        String dependedFileName = dependedFileNameWithVersion.substring(0, i);

        JarFile file = new JarFile(new File(fileName));
        DeploymentDirectory dir = new JarDeploymentRoot(file);

        ImportOrderFile files = new ImportOrderFileDiscoverer().getFilesToImport(dir);

        String firstDependency = files.getDependencies().iterator().next();

        assertEquals(1, files.getDependencies().size());
        assertEquals(dependedFileName, firstDependency);
    }
}
