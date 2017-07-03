/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.util.ims.extension;

import java.util.Collection;

import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.common.filesystem.handle.StagingFile;

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
