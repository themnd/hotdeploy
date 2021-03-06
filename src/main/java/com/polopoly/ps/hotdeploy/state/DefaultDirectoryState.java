package com.polopoly.ps.hotdeploy.state;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.polopoly.ps.hotdeploy.HotDeployContentContextListener;
import com.polopoly.ps.hotdeploy.file.DeploymentFile;

public class DefaultDirectoryState implements DirectoryState {
	private FileChecksums fileChecksums;

	/**
	 * Contains the FileStates of failed imports. We don't save these since they
	 * should be retried on the next redeploy.
	 */
	private Map<DeploymentFile, FileState> failedFileStateByFile = new HashMap<DeploymentFile, FileState>();

	private Set<DeploymentFile> successfulResets = new HashSet<DeploymentFile>();

	public DefaultDirectoryState(FileChecksums checksums) {
		this.fileChecksums = checksums;
	}

	/**
	 * Checks whether the file has changed on disk relative to the state it had
	 * when it was last imported by the {@link HotDeployContentContextListener}.
	 * If it had never been imported by it, true is returned.
	 */
	public boolean hasFileChanged(DeploymentFile file) {
		return getOldState(file).hasFileChanged(file);
	}

	private FileState getOldState(DeploymentFile file) {
		FileState result = failedFileStateByFile.get(file);

		if (result == null) {
			long quickChecksum = fileChecksums.getQuickChecksum(file);
			long slowChecksum = fileChecksums.getSlowChecksum(file);

			result = new FileState(quickChecksum, slowChecksum);
		}

		return result;
	}

	public void reset(DeploymentFile file, boolean failed) {
		if (failed) {
			failedFileStateByFile.put(
					file,
					new FileState(file.getQuickChecksum(), file
							.getSlowChecksum()));
			successfulResets.remove(file);
		} else {
			failedFileStateByFile.remove(file);
			successfulResets.add(file);
			fileChecksums.setChecksums(file, file.getQuickChecksum(),
					file.getSlowChecksum());
		}
	}

	public void setFileChecksums(FileChecksums fileChecksums) {
		this.fileChecksums = fileChecksums;
	}

	public boolean areAllChangesPersisted() {
		return fileChecksums.areAllChangesPersisted();
	}

	public void persist() throws CouldNotUpdateStateException {
		fileChecksums.persist();
	}

	public FileChecksums getFileChecksums() {
		return fileChecksums;
	}

	public Set<DeploymentFile> getSuccessfulResets() {
		return successfulResets;
	}

	public Set<DeploymentFile> getFailedResets() {
		return failedFileStateByFile.keySet();
	}

	@Override
	public void forget(DeploymentFile file) {
		fileChecksums.deleteChecksums(file);
		failedFileStateByFile.remove(file);
		successfulResets.remove(file);
	}
}
