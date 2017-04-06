package com.tle.core.util.ims.extension;

import java.util.Collection;

import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.core.filesystem.StagingFile;

/**
 * @author Aaron
 */
public interface IMSFileExporter
{
	/**
	 * Copies necessary files from the item to the staging
	 * 
	 * @param info This is actually a SectionInfo but cannot be declared here as
	 *            such!
	 * @param imsRoot
	 * @param item
	 */
	void exportFiles(Object info, Item item, StagingFile imsRoot);

	/**
	 * Copies necessary files from the staging to the item
	 * 
	 * @param info This is actually a SectionInfo but cannot be declared here as
	 *            such!
	 * @param item
	 * @param staging
	 * @param packageExtractedFolder This is where the files are currently
	 *            residing. It is NOT (necessarily) a final extracted path
	 * @param packageName The name of the package zip folder e.g. package.zip
	 */
	void importFiles(Item item, StagingFile staging, String packageExtractedFolder, String packageName,
		Collection<Attachment> createdAttachments);
}
