package example.deploy.xml.ordergenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import example.deploy.hotdeploy.file.DeploymentObject;
import example.deploy.hotdeploy.file.FileDeploymentDirectory;
import example.deploy.hotdeploy.file.FileDeploymentFile;
import example.deploy.xml.ordergenerator.RootDirectoryFinder.NoRootDirectoryException;

public class TestRootDirectoryFinder extends TestCase {
    public void testNoFile() throws NoRootDirectoryException {
        List<DeploymentObject> files = new ArrayList<DeploymentObject>();

        try {
            File root = new RootDirectoryFinder(files).findRootDirectory();

            fail("Found non-existing root directory " + root);
        } catch (NoRootDirectoryException e) {
        }
    }

    public void testSingleFile() throws NoRootDirectoryException {
        List<DeploymentObject> files = new ArrayList<DeploymentObject>();

        files.add(new FileDeploymentFile(new File("/hej")));

        assertEquals(new File("/"), new RootDirectoryFinder(files).findRootDirectory());
    }

    public void testTwoFilesSameDirectory() throws NoRootDirectoryException {
        List<DeploymentObject> files = new ArrayList<DeploymentObject>();

        files.add(new FileDeploymentFile(new File("/a/b")));
        files.add(new FileDeploymentFile(new File("/a/c")));

        assertEquals(new File("/a"), new RootDirectoryFinder(files).findRootDirectory());
    }

    public void testFileAndDirectory() throws NoRootDirectoryException {
        List<DeploymentObject> files = new ArrayList<DeploymentObject>();

        files.add(new FileDeploymentFile(new File("/a/b")));
        files.add(new FileDeploymentDirectory(new File("/a")));

        assertEquals(new File("/a"), new RootDirectoryFinder(files).findRootDirectory());
    }

    public void testSameDirectoryTwice() throws NoRootDirectoryException {
        List<DeploymentObject> files = new ArrayList<DeploymentObject>();

        files.add(new FileDeploymentDirectory(new File("/a/b")));
        files.add(new FileDeploymentDirectory(new File("/a/b")));

        assertEquals(new File("/a/b"), new RootDirectoryFinder(files).findRootDirectory());
    }

    public void testTwoFilesDifferentDirectory() throws NoRootDirectoryException {
        List<DeploymentObject> files = new ArrayList<DeploymentObject>();

        files.add(new FileDeploymentFile(new File("/a/b/d")));
        files.add(new FileDeploymentFile(new File("/a/c")));

        assertEquals(new File("/a"), new RootDirectoryFinder(files).findRootDirectory());
    }

    public void testRootisRoot() throws NoRootDirectoryException {
        List<DeploymentObject> files = new ArrayList<DeploymentObject>();

        files.add(new FileDeploymentFile(new File("/b")));
        files.add(new FileDeploymentFile(new File("/a/c")));

        assertEquals(new File("/"), new RootDirectoryFinder(files).findRootDirectory());
    }

    public void testRelativePathNoRoot() throws NoRootDirectoryException {
        List<DeploymentObject> files = new ArrayList<DeploymentObject>();

        files.add(new FileDeploymentFile(new File("b")));
        files.add(new FileDeploymentFile(new File("c")));

        assertEquals(new File("").getAbsoluteFile(), new RootDirectoryFinder(files).findRootDirectory());
    }

    public void testRelativePath() throws NoRootDirectoryException {
        List<DeploymentObject> files = new ArrayList<DeploymentObject>();

        files.add(new FileDeploymentFile(new File("a/b")));
        files.add(new FileDeploymentFile(new File("a/c")));

        assertEquals(new File("a").getAbsoluteFile(), new RootDirectoryFinder(files).findRootDirectory());
    }
}
