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

package com.tle.common.wizard.controls.universal.handlers;

import java.util.List;

import com.dytech.edge.wizard.beans.control.CustomControl;
import com.tle.common.wizard.controls.universal.UniversalSettings;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public class FileUploadSettings extends UniversalSettings
{
	private static final String NOUNZIP = "FILE_NOUNZIP";
	private static final String PACKAGEONLY = "FILE_PACKAGEONLY";
	private static final String QTIPACKAGE = "FILE_QTI_PACKAGE";
	private static final String SCORMPACKAGE = "FILE_SCORM_PACKAGE";
	private static final String SUPPRESSTHUMBS = "FILE_SUPPRESS_THUMBS";
	private static final String SHOWTHUMBOPTION = "FILE_SHOW_THUMB_OPT";
	private static final String RESTRICTMIME = "FILE_RESTRICTMIME";
	private static final String KEY_MIMETYPES = "MIMETYPES";
	private static final String RESTRICTFILESIZE = "FILE_RESTRICTSIZE";
	private static final String MAXFILESIZE = "FILE_MAXFILESIZE";

	public FileUploadSettings(CustomControl wrapped)
	{
		super(wrapped);
	}

	public FileUploadSettings(UniversalSettings settings)
	{
		super(settings.getWrapped());
	}

	public boolean isNoUnzip()
	{
		return wrapped.getBooleanAttribute(NOUNZIP, false);
	}

	public void setNoUnzip(boolean noUnzip)
	{
		wrapped.getAttributes().put(NOUNZIP, noUnzip);
	}

	public boolean isPackagesOnly()
	{
		return wrapped.getBooleanAttribute(PACKAGEONLY, false);
	}

	public void setPackagesOnly(boolean packagesOnly)
	{
		wrapped.getAttributes().put(PACKAGEONLY, packagesOnly);
	}

	public boolean isQtiPackagesOnly()
	{
		return wrapped.getBooleanAttribute(QTIPACKAGE, false);
	}

	public void setQtiPackagesOnly(boolean qtiOnly)
	{
		wrapped.getAttributes().put(QTIPACKAGE, qtiOnly);
	}

	public boolean isScormPackagesOnly()
	{
		return wrapped.getBooleanAttribute(SCORMPACKAGE, false);
	}

	public void setScormPackagesOnly(boolean scormOnly)
	{
		wrapped.getAttributes().put(SCORMPACKAGE, scormOnly);
	}

	public boolean isSuppressThumbnails()
	{
		return wrapped.getBooleanAttribute(SUPPRESSTHUMBS, false);
	}

	public void setSuppressThumbnails(boolean suppress)
	{
		wrapped.getAttributes().put(SUPPRESSTHUMBS, suppress);
	}

	public boolean isShowThumbOption()
	{
		return wrapped.getBooleanAttribute(SHOWTHUMBOPTION, false);
	}

	public void setShowThumbOption(boolean showOption)
	{
		wrapped.getAttributes().put(SHOWTHUMBOPTION, showOption);
	}

	public boolean isRestrictByMime()
	{
		return wrapped.getBooleanAttribute(RESTRICTMIME, false);
	}

	public void setRestrictByMime(boolean restrictMime)
	{
		wrapped.getAttributes().put(RESTRICTMIME, restrictMime);
	}

	public List<String> getMimeTypes()
	{
		return wrapped.ensureListAttribute(KEY_MIMETYPES);
	}

	public void setMimeTypes(List<String> mimeTypes)
	{
		wrapped.getAttributes().put(KEY_MIMETYPES, mimeTypes);
	}

	public boolean isRestrictFileSize()
	{
		return wrapped.getBooleanAttribute(RESTRICTFILESIZE, false);
	}

	public void setRestrictFileSize(boolean restrictFileSize)
	{
		wrapped.getAttributes().put(RESTRICTFILESIZE, restrictFileSize);
	}

	public int getMaxFileSize()
	{
		Integer maxFileSize = (Integer) wrapped.getAttributes().get(MAXFILESIZE);
		if( maxFileSize != null )
		{
			return maxFileSize;
		}
		return 0;
	}

	public void setMaxFileSize(int maxFileSize)
	{
		wrapped.getAttributes().put(MAXFILESIZE, maxFileSize);
	}

	// public boolean isNoScrapbook()
	// {
	// return wrapped.getBooleanAttribute(NOSCRAPBOOK, false);
	// }
	//
	// public void setNoScrapbook(boolean noScrapbook)
	// {
	// wrapped.getAttributes().put(NOUNZIP, noScrapbook);
	// }
}
