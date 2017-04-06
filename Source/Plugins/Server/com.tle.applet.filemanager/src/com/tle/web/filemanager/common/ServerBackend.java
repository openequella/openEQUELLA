package com.tle.web.filemanager.common;

import java.net.URL;
import java.util.List;

public interface ServerBackend
{
	URL getDownloadUrl(String wizardId, String filename);

	void delete(String wizardId, String filename);

	List<FileInfo> listFiles(String wizardId, String directory);

	/**
	 * @return true for a successful rename.
	 */
	boolean renameFile(String wizardId, String oldName, String newName);

	void copy(String wizardId, String sourceFile, String destFile);

	void markAsResource(String wizardId, boolean mark, String fullPath);

	void newFolder(String wizardId, String name);

	void write(String wizardId, String filename, boolean append, byte[] upload);

	void extractArchive(String wizardId, String filename, String destDir);
}
