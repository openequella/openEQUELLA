package com.tle.core.activation.migration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.common.io.UnicodeReader;
import com.dytech.devlib.PropBagEx;
import com.dytech.devlib.PropBagEx.PropBagIterator;
import com.dytech.edge.common.Constants;
import com.tle.core.activation.ActivationsConverter;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.migration.AbstractItemXmlMigrator;
import com.tle.core.services.FileSystemService;

@Bind
@Singleton
public class MigrateActivations extends AbstractItemXmlMigrator
{

	@Inject
	private FileSystemService fileSystemService;

	@SuppressWarnings("nls")
	@Override
	public boolean migrate(ConverterParams params, PropBagEx xml, SubTemporaryFile file, String filename)
		throws Exception
	{
		String id = filename.substring(0, filename.indexOf('.'));
		String activationsFilename = id + "-extra/" + ActivationsConverter.ACTIVATIONS_XML; //$NON-NLS-1$

		if( fileSystemService.fileExists(file, activationsFilename) )
		{
			InputStream inp = fileSystemService.read(file, activationsFilename);
			PropBagEx actXml = new PropBagEx(new UnicodeReader(inp, Constants.UTF8));
			PropBagIterator iter = actXml.iterator("com.tle.beans.cal.ActivateRequest"); //$NON-NLS-1$
			while( iter.hasNext() )
			{
				PropBagEx req = iter.next();
				req.setNodeName("com.tle.beans.activation.ActivateRequest");
				req.setNode("type", "cal");
			}

			ByteArrayInputStream bais = new ByteArrayInputStream(actXml.toString().getBytes(Constants.UTF8));
			fileSystemService.write(file, activationsFilename, bais, false);
			inp.close();
		}
		return false;
	}

}
