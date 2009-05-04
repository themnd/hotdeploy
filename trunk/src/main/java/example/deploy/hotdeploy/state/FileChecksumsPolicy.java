package example.deploy.hotdeploy.state;

import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.server.ServerNames;
import com.polopoly.cm.xml.hotdeploy.util.LongComponent;

import example.deploy.hotdeploy.file.DeploymentFile;

@SuppressWarnings("deprecation")
public class FileChecksumsPolicy extends ContentPolicy {
    private static final String QUICK_CHECKSUM_COMPONENT = "quick";
    private static final String SLOW_CHECKSUM_COMPONENT = "slow";

    private LongComponent getQuickChecksumComponent(DeploymentFile file) {
        return new LongComponent(file.getName(),
            QUICK_CHECKSUM_COMPONENT, -1);
    }

    private LongComponent getSlowChecksumComponent(DeploymentFile file) {
        return new LongComponent(file.getName(),
            SLOW_CHECKSUM_COMPONENT, -1);
    }

    @Override
    public void postCreateSelf() throws CMException {
        if (getName() == null) {
            setName("Hot Deploy Content State");

            getContentUnwrapped().setMetaDataComponent(
                    ServerNames.CMD_ATTRG_SYSTEM,
                    ServerNames.CMD_ATTR_MAXVERSIONS,
                    "1");
        }
    }

    public long getQuickChecksum(DeploymentFile file) {
        return getQuickChecksumComponent(file).getLongValue(this);
    }

    public long getSlowChecksum(DeploymentFile file) {
        return getSlowChecksumComponent(file).getLongValue(this);
    }

    public void setChecksums(DeploymentFile file, long quickChecksum,
            long slowChecksum) throws CouldNotUpdateStateException {
        try {
            getQuickChecksumComponent(file).setLongValue(this, quickChecksum);
            getSlowChecksumComponent(file).setLongValue(this, slowChecksum);
        } catch (CMException e) {
            throw new CouldNotUpdateStateException("While saving deployment state of " + file + ": " + e, e);
        }
    }
}
