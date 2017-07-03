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

package com.tle.ims.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.edge.ejb.helpers.metadata.mappers.AbstractXPathPackageMapper;
import com.google.common.base.Throwables;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.ims.service.IMSService;

@Bind
@Singleton
public class IMSMetadataMapper extends AbstractXPathPackageMapper
{
	@Inject
	private IMSService imsService;

	@Override
	public boolean isSupportedPackage(FileHandle handle, String packageExtractedFolder)
	{
		try( InputStream inp = imsService.getImsManifestAsStream(handle, packageExtractedFolder, false) )
		{
			return inp != null;
		}
		catch( IOException e )
		{
			throw Throwables.propagate(e);
		}
	}

	@Override
	protected InputStream getXmlStream(FileHandle handle, String packageExtractedFolder) throws IOException
	{
		return imsService.getImsManifestAsStream(handle, packageExtractedFolder, true);
	}

	@Override
	public List<String> getSupportedFormatsForDisplay()
	{
		return Arrays.asList(new String[]{CurrentLocale.get("com.tle.web.ims.imspackage"), //$NON-NLS-1$
				CurrentLocale.get("com.tle.web.ims.scormpackage")}); //$NON-NLS-1$
	}
}
