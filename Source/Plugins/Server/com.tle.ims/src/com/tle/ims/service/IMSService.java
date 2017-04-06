package com.tle.ims.service;

import java.io.IOException;
import java.io.InputStream;

import com.tle.annotation.Nullable;
import com.tle.beans.filesystem.FileHandle;
import com.tle.core.util.ims.beans.IMSManifest;

public interface IMSService
{
	String getScormVersion(FileHandle stagingHandle, String pagename);

	/**
	 * Retrieve the title for an IMS package.
	 */
	String getImsTitle(FileHandle handle, String packageName);

	/**
	 * Retrieve the rights description for an IMS package.
	 */
	String getImsRightsDescription(FileHandle handle, String packageName) throws IOException;

	InputStream retrieveIMSPackage(FileHandle handle, String packageExtractedFolder) throws IOException;

	void ensureIMSPackage(FileHandle handle, String packageExtractedFolder) throws IOException;

	/**
	 * Retrieve the IMS manifest for an IMS package. Due to SCORM extensions,
	 * manifest may be split up into several files. Using the manifest returned
	 * for this method will guarantee that the manifest is combined into a
	 * single entity.
	 * 
	 * @param handle The base directory handle
	 * @param packageName the name of the ims package
	 */
	InputStream getImsManifestAsStream(FileHandle handle, String packageExtractedFolder, boolean logNotFound)
		throws IOException;

	/**
	 * @param handle
	 * @param packageExtractedFolder
	 * @param manifestName Leave null in the case of packageExtractedFolder
	 *            being the actual XML file
	 * @param logNotFound
	 * @return
	 * @throws IOException
	 */
	InputStream getMetsManifestAsStream(FileHandle handle, String packageExtractedFolder,
		@Nullable String manifestName, boolean logNotFound) throws IOException;

	/**
	 * Retrieve the IMS manifest for an IMS package as a bean hierarchy. Due to
	 * SCORM extensions, manifest may be split up into several files. Using the
	 * manifest returned for this method will guarantee that the manifest is
	 * combined into a single entity.
	 * 
	 * @param handle The base directory handle
	 * @param packageName the name of the ims package
	 */
	IMSManifest getImsManifest(FileHandle handle, String packageExtractedFolder, boolean logNotFound)
		throws IOException;

	/**
	 * Retrieve the IMS manifest for an IMS package as a bean hierarchy. Due to
	 * SCORM extensions, manifest may be split up into several files. Using the
	 * manifest returned for this method will guarantee that the manifest is
	 * combined into a single entity.
	 * 
	 * @param in XML input stream
	 */
	IMSManifest getImsManifest(InputStream in) throws IOException;

}
