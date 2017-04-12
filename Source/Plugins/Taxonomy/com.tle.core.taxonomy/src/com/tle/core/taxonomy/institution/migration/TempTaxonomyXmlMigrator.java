package com.tle.core.taxonomy.institution.migration;

import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.migration.XmlMigrator;

@Bind
@Singleton
@SuppressWarnings("nls")
public class TempTaxonomyXmlMigrator extends XmlMigrator
{
	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
	{
		SubTemporaryFile itemdefFolder = new SubTemporaryFile(staging, "itemdefinition");
		for( String entry : xmlHelper.getXmlFileList(itemdefFolder) )
		{
			PropBagEx xml = xmlHelper.readToPropBagEx(itemdefFolder, entry);

			boolean changed = false;
			for( PropBagEx cxml : xml
				.iterateAll("slow/wizard/pages/*/controls/com.dytech.edge.wizard.beans.control.PickList") )
			{
				cxml.setNodeName("com.dytech.edge.wizard.beans.control.CustomControl");
				cxml.setNode("classType", "termselector");

				PropBagEx as = cxml.newSubtree("attributes");
				newEntry(as, "KEY_TERM_STORAGE_FORMAT", "FULL_PATH");
				newEntry(as, "RELOAD_PAGE_ON_SELECTION", false);
				newEntry(as, "KEY_SELECTED_TAXONOMY", cxml.getNode("taxonomy"));
				newEntry(as, "KEY_DISPLAY_TYPE", "popupBrowser");
				newEntry(as, "KEY_ALLOW_MULTIPLE", cxml.isNodeTrue("multiple"));
				newEntry(as, "KEY_SELECTION_RESTRICTION", "UNRESTRICTED");
				newEntry(as, "KEY_ALLOW_ADD_TERMS", false);

				cxml.deleteNode("taxonomy");
				cxml.deleteNode("multiple");
				cxml.deleteNode("controls");

				changed = true;
			}

			if( changed )
			{
				xmlHelper.writeFromPropBagEx(itemdefFolder, entry, xml);
			}
		}
	}

	private void newEntry(PropBagEx attributes, String key, Object value)
	{
		PropBagEx entry = attributes.newSubtree("entry");
		entry.createNode("string", key);
		entry.createNode(value instanceof Boolean ? "boolean" : "string", value.toString());
	}
}
