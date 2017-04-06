package com.tle.cal.migration;

import java.util.List;

import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.entity.LanguageBundle;
import com.tle.common.i18n.LangUtils;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.migration.XmlMigrator;

@Bind
@Singleton
public class CalPageMigration extends XmlMigrator
{
	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
	{
		SubTemporaryFile folder = new SubTemporaryFile(staging, "itemdefinition"); //$NON-NLS-1$
		final List<String> entries = xmlHelper.getXmlFileList(folder);
		for( String entry : entries )
		{
			PropBagEx xml = xmlHelper.readToPropBagEx(folder, entry);

			// There should only be one of the following paths, but we need to
			// iterate to
			// it to be sure that we find it
			PropBagEx page = xml.getSubtree("slow/wizard/pages/com.dytech.edge.wizard.beans.CALPage"); //$NON-NLS-1$
			if( page != null )
			{
				PropBagEx attrs = xml.aquireSubtree("attributes"); //$NON-NLS-1$

				addAttribute(attrs, "com.tle.cal-Enabled", Boolean.TRUE.toString()); //$NON-NLS-1$

				addAttribute(attrs, "com.tle.cal-ActivationError", //$NON-NLS-1$
					getLanguageBundleXmlAsLangString(page.getSubtree("error"))); //$NON-NLS-1$
				addAttribute(attrs, "com.tle.cal-InactiveError", //$NON-NLS-1$
					getLanguageBundleXmlAsLangString(page.getSubtree("inactiveError"))); //$NON-NLS-1$

				if( Boolean.valueOf(page.getNode("hasAgreement")) ) //$NON-NLS-1$
				{
					addAttribute(attrs, "com.tle.cal-HasAgreement", Boolean.TRUE.toString()); //$NON-NLS-1$
				}
				addAttribute(attrs, "com.tle.cal-AgreementFile", page.getNode("agreement")); //$NON-NLS-1$ //$NON-NLS-2$

				// Delete the old version
				xml.deleteAll("slow/wizard/pages/com.dytech.edge.wizard.beans.CALPage"); //$NON-NLS-1$

				xmlHelper.writeFromPropBagEx(folder, entry, xml);
			}
		}
	}

	private void addAttribute(PropBagEx attrs, String key, String value)
	{
		PropBagEx attr = attrs.newSubtree("com.tle.beans.entity.BaseEntity-Attribute"); //$NON-NLS-1$
		attr.setNode("key", key); //$NON-NLS-1$
		attr.setNode("value", value); //$NON-NLS-1$
	}

	private String getLanguageBundleXmlAsLangString(PropBagEx xml)
	{
		if( xml == null )
		{
			return ""; //$NON-NLS-1$
		}

		xml.setNodeName("com.tle.beans.entity.LanguageBundle"); //$NON-NLS-1$
		LanguageBundle bundle = (LanguageBundle) xmlHelper.readXmlString(xml.toString());

		return LangUtils.getBundleAsXmlString(bundle);
	}
}
