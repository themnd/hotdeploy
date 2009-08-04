package example.deploy.hotdeploy.deployer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentIdFactory;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.client.impl.exceptions.LockException;
import com.polopoly.cm.client.impl.exceptions.PermissionDeniedException;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.xml.io.DispatchingDocumentImporter;
import com.polopoly.common.xml.DOMUtil;

import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.file.FileDeploymentFile;
import example.deploy.hotdeploy.file.JarDeploymentFile;

public class DefaultSingleFileDeployer implements SingleFileDeployer {
    private static final Logger logger =
        Logger.getLogger(DefaultSingleFileDeployer.class.getName());
    private PolicyCMServer server;
    private DispatchingDocumentImporter importer;

    public DefaultSingleFileDeployer(PolicyCMServer server) {
        this.server = server;
    }

    public void prepare() throws ParserConfigurationException {
        importer = new DispatchingDocumentImporter(server);
    }

    private void setImporterBaseUrl(DispatchingDocumentImporter importer,
            DeploymentFile fileToImport) {
        try {
            URL baseUrl = fileToImport.getBaseUrl();

            if (logger.isLoggable(Level.FINEST)) {
                logger.log(Level.FINEST, "Using base URL " + baseUrl);
            }

            importer.setBaseUrl(baseUrl);
        }
        catch (MalformedURLException e) {
            logger.log(Level.WARNING, "Failed to create base URL for file " + fileToImport, e);
        }
    }

    private void importFile(DeploymentFile fileToImport) throws Exception {
        setImporterBaseUrl(importer, fileToImport);

        DocumentBuilder documentBuilder = DOMUtil.newDocumentBuilder();
        Document xmlDocument =
            documentBuilder.parse(fileToImport.getInputStream());

        if (fileToImport instanceof JarDeploymentFile) {
            importer.importXML(
                xmlDocument,
                ((JarDeploymentFile) fileToImport).getJarFile(),
                ((JarDeploymentFile) fileToImport).getNameWithinJar());
        }
        else {
            importer.importXML(
                xmlDocument,
                null,
                ((FileDeploymentFile) fileToImport).getDirectory());
        }
    }

    public boolean importAndHandleException(DeploymentFile fileToImport) throws FatalDeployException {
        if (importer == null) {
            throw new FatalDeployException("prepare() must be called before import.");
        }

        try {
            importFile(fileToImport);

            logger.log(Level.INFO, "Import of " + fileToImport + " done.");

            return true;
        }
        catch (PermissionDeniedException e) {
            throw new FatalDeployException(e);
        }
        catch (ParserConfigurationException e) {
            throw new FatalDeployException(e);
        }
        catch (LockException e) {
            ContentId lockedId = null;

            if (e.getLockInfo() != null) {
                lockedId = e.getLockInfo().getLocked();
            }
            else {
                int i = e.getMessage().indexOf("ContentId(");

                if (i != -1) {
                    int j = e.getMessage().indexOf(")", i+1);

                    String contentIdString = e.getMessage().substring(i + 10, j);

                    try {
                        lockedId = ContentIdFactory.createContentId(contentIdString);
                    }
                    catch (IllegalArgumentException iae) {
                        logger.log(Level.WARNING, "Could not parse content ID \"" + contentIdString + "\"in error message.");
                    }
                }
            }

            if (lockedId == null) {
                logger.log(Level.WARNING,
                    "Import of " + fileToImport + " failed: " + e.getMessage());

                return false;
            }

            try {
                Content content = (Content) server.getContent(lockedId);

                logger.log(Level.WARNING, lockedId.getContentIdString() +
                    " was locked. Trying to unlock it.");

                content.forcedUnlock();

                logger.log(Level.INFO, "Retrying import...");

                return importAndHandleException(fileToImport);
            } catch (CMException cmException) {
                logger.log(Level.WARNING,
                    "Import of " + fileToImport + " failed: " + e.getMessage());

                return false;
            }
        }
        catch (Exception e) {
            logger.log(Level.WARNING,
                "Import of " + fileToImport + " failed: " + e.getMessage(), e);

            return false;
        }
    }
}