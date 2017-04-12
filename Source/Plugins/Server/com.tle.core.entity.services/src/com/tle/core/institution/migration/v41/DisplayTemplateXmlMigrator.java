package com.tle.core.institution.migration.v41;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.thoughtworks.xstream.XStream;
import com.tle.beans.entity.itemdef.DisplayTemplate;
import com.tle.beans.entity.itemdef.SummaryDisplayTemplate;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.migration.XmlMigrator;
import com.tle.core.xstream.XmlService;

@Bind
@Singleton
@SuppressWarnings("nls")
public class DisplayTemplateXmlMigrator extends XmlMigrator
{
	@Inject
	private XmlService xmlService;
	@Inject
	private DisplayTemplateMigration displayTemplateMigration;

	private XStream xstream;

	@PostConstruct
	protected void setupXStream()
	{
		xstream = xmlService.createDefault(getClass().getClassLoader());
		xstream.alias("itemSummaryTemplate", DisplayTemplate.class);
	}

	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
	{
		final SubTemporaryFile itemDefFolder = new SubTemporaryFile(staging, "itemdefinition");
		for( String entry : xmlHelper.getXmlFileList(itemDefFolder) )
		{
			PropBagEx itemDef = xmlHelper.readToPropBagEx(itemDefFolder, entry);

			PropBagEx summaryTemplate = itemDef.getSubtree("slow/itemSummaryTemplate");
			DisplayTemplate oldTemplate = new DisplayTemplate();
			if( summaryTemplate != null )
			{
				oldTemplate = (DisplayTemplate) xstream.fromXML(summaryTemplate.toString(), oldTemplate);
			}
			SummaryDisplayTemplate newTemplate = displayTemplateMigration.convertToNew(oldTemplate);
			String newXml = xstream.toXML(new TempBlobs(newTemplate));
			itemDef.deleteNode("slow/itemSummaryTemplate");
			itemDef.appendChildren("slow", new PropBagEx(newXml));
			xmlHelper.writeFromPropBagEx(itemDefFolder, entry, itemDef);
		}
	}

	public static class TempBlobs
	{
		public TempBlobs(SummaryDisplayTemplate newTemplate)
		{
			// xstream will use reflection to get at the internals
			this.itemSummarySections = newTemplate; // NOSONAR
		}

		SummaryDisplayTemplate itemSummarySections;
	}
}
