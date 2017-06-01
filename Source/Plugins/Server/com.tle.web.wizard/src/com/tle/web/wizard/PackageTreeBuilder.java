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

package com.tle.web.wizard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.tle.beans.filesystem.FileHandle;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.web.sections.SectionInfo;

/**
 * @author aholland
 */
public interface PackageTreeBuilder
{
	class PackageInfo
	{
		boolean isValid;
		protected String error;

		protected String title;
		protected String scormVersion;

		protected Collection<Attachment> createdAttachments;

		public boolean isValid()
		{
			return isValid;
		}

		public void setValid(boolean isValid)
		{
			this.isValid = isValid;
		}

		public String getError()
		{
			return error;
		}

		public void setError(String error)
		{
			this.error = error;
		}

		public String getTitle()
		{
			return title;
		}

		public void setTitle(String title)
		{
			this.title = title;
		}

		public String getScormVersion()
		{
			return scormVersion;
		}

		public void setScormVersion(String scormVersion)
		{
			this.scormVersion = scormVersion;
		}

		public Collection<Attachment> getCreatedAttachments()
		{
			if( createdAttachments == null )
			{
				createdAttachments = new ArrayList<Attachment>();
			}
			return createdAttachments;
		}

		public void setCreatedAttachments(Collection<Attachment> createdAttachments)
		{
			this.createdAttachments = createdAttachments;
		}
	}

	/**
	 * @param item
	 * @param staging
	 * @param packageExtractedFolder This is where the files are currently
	 *            residing. It is NOT (necessarily) a final extracted path. e.g.
	 *            _uploads/_IMS/package.zip NOTE: this will be null in the case
	 *            of non-zip packages
	 * @param originalPackagePath This where the original file was uploaded to
	 *            e.g. _uploads/package.zip or _uploads/mets.xml
	 * @param packageName The name of the package zip folder e.g. package.zip It
	 *            is the folder name of where the files when end up when
	 *            committed (or the filename of a non zip manifest e.g.
	 *            mets.xml)
	 * @param expand
	 * @return
	 */
	PackageInfo createTree(Item item, FileHandle staging, String packageExtractedFolder, String originalPackagePath,
		String packageName, boolean expand);

	/**
	 * @param staging
	 * @param packageExtractedFolder
	 * @return
	 */
	PackageInfo getInfo(SectionInfo info, FileHandle staging, String packageExtractedFolder);

	/**
	 * Determines if this is a recognisable package without fully unzipping the
	 * file
	 * 
	 * @param staging
	 * @param packageFilepath
	 * @return
	 */
	boolean canHandle(SectionInfo info, FileHandle staging, String packageFilepath);

	/**
	 * Assumes what you are passing is valid for this builder. Ie. call
	 * canHandle before invoking this.
	 * 
	 * @param staging
	 * @param packageFilepath
	 * @return Typically one of: "METS", "IMS", "QTITEST", "SCORM". Note that
	 *         IMS packages could be "IMS" or "SCORM"+"IMS" etc. Also note that
	 *         the most specific type is returned first, e.g QTITEST is more
	 *         specific than IMS.
	 */
	List<String> determinePackageTypes(SectionInfo info, FileHandle staging, String packageFilepath);
}
