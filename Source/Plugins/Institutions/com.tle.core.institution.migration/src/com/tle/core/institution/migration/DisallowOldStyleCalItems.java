package com.tle.core.institution.migration;

import java.util.Objects;

import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AbstractItemXmlMigrator;
import com.tle.core.institution.convert.ConverterParams;

@Bind
@Singleton
@SuppressWarnings("nls")
public class DisallowOldStyleCalItems extends AbstractItemXmlMigrator
{
	@Override
	public boolean migrate(ConverterParams params, PropBagEx xml, SubTemporaryFile file, String filename)
		throws Exception
	{
		if( Objects.equals("Cradle", params.getBranchString()) )
		{
			if( xml.nodeExists("cal") )
			{
				throw new Exception("CAL item can not be migrated: items/" + filename);
			}
		}
		else if( Objects.equals("Cradle2", params.getBranchString()) )
		{
			xml.deleteNode("requests");
			for( PropBagEx activate : xml.iterateAll("activateRequests/*") )
			{
				activate.deleteNode("courseOld");
			}
			return true;
		}
		return false;
	}
}
