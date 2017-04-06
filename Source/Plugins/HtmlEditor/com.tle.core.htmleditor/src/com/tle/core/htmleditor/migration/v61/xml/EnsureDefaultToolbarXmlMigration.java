package com.tle.core.htmleditor.migration.v61.xml;

import java.io.IOException;
import java.util.Map;

import javax.inject.Singleton;

import com.tle.common.NameValue;
import com.tle.core.guice.Bind;
import com.tle.core.htmleditor.migration.v61.EnsureDefaultToolbarMigration;
import com.tle.core.institution.migration.PostReadMigrator;

@Bind
@Singleton
public class EnsureDefaultToolbarXmlMigration implements PostReadMigrator<Map<String, String>>
{
	@SuppressWarnings("nls")
	@Override
	public void migrate(Map<String, String> obj) throws IOException
	{
		// I don't think it likely that the toolbar is blank on purpose
		if( !obj.containsKey("htmleditor.toolbar.rows.0.buttons.0")
			&& !obj.containsKey("htmleditor.toolbar.rows.1.buttons.0")
			&& !obj.containsKey("htmleditor.toolbar.rows.2.buttons.0") )
		{
			for( NameValue nv : EnsureDefaultToolbarMigration.DEFAULT_TOOLBAR )
			{
				obj.put(nv.getName(), nv.getValue());
			}
		}
	}
}
