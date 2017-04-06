package com.tle.ims.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.edge.ejb.helpers.metadata.mappers.AbstractXPathPackageMapper;
import com.google.common.base.Throwables;
import com.tle.beans.filesystem.FileHandle;
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
