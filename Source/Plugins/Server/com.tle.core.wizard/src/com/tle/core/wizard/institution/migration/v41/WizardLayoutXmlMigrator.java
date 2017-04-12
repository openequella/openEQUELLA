package com.tle.core.wizard.institution.migration.v41;

import java.util.List;

import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.migration.XmlMigrator;
import com.tle.core.util.DefaultMessageCallback;

/**
 * @author aholland
 */
@Bind
@Singleton
@SuppressWarnings("nls")
public class WizardLayoutXmlMigrator extends XmlMigrator
{
	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
	{
		final SubTemporaryFile itemdefFolder = new SubTemporaryFile(staging, "itemdefinition");
		final List<String> entries = xmlHelper.getXmlFileList(itemdefFolder);

		DefaultMessageCallback message = new DefaultMessageCallback(
			"com.tle.core.wizard.institution.migration.v41.layoutmigrator.progressmessage");
		params.setMessageCallback(message);
		message.setType(CurrentLocale.get("com.tle.core.wizard.wizard"));
		message.setTotal(entries.size());

		for( String collection : entries )
		{
			PropBagEx itemDef = xmlHelper.readToPropBagEx(itemdefFolder, collection);

			PropBagEx wizard = itemDef.getSubtree("slow/wizard");
			if( wizard != null )
			{
				// The latest version of the code removes the layout, so we
				// won't even bother updating the XML.

				// String layout = wizard.getNode("layout");
				// if( Check.isEmpty(layout) )
				// {
				// wizard.setNode("layout",
				// WizardConstants.DEFAULT_WIZARD_LAYOUT);
				// }

				// Not strictly necessary...
				String allowNonSequentialNavigation = wizard.getNode("allowNonSequentialNavigation");
				if( Check.isEmpty(allowNonSequentialNavigation) )
				{
					wizard.setNode("allowNonSequentialNavigation", "false");
				}
			}
			xmlHelper.writeFromPropBagEx(itemdefFolder, collection, itemDef);
			message.incrementCurrent();
		}
	}
}
