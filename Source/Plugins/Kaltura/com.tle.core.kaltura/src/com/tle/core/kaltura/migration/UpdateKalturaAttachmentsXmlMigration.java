package com.tle.core.kaltura.migration;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.w3c.dom.Node;

import com.dytech.devlib.PropBagEx;
import com.tle.common.Check;
import com.tle.common.kaltura.KalturaUtils;
import com.tle.common.kaltura.entity.KalturaServer;
import com.tle.common.util.XmlDocument;
import com.tle.common.util.XmlDocument.NodeListIterable;
import com.tle.core.filesystem.ImportFile;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.XmlHelper;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.migration.AbstractItemXmlMigrator;

@Bind
@Singleton
@SuppressWarnings("nls")
public class UpdateKalturaAttachmentsXmlMigration extends AbstractItemXmlMigrator
{
	@Inject
	private XmlHelper xmlHelper;

	@Override
	public boolean migrate(ConverterParams params, PropBagEx xml, SubTemporaryFile file, String filename)
		throws Exception
	{
		boolean modified = false;

		XmlDocument itemXml = new XmlDocument(xml.toString());
		if( itemXml
			.node("com.tle.beans.item.Item/attachments/com.tle.beans.item.attachments.CustomAttachment[value1=\"kaltura\"]") != null )
		{
			System.out.print(true);
			NodeListIterable kalturaAttachments = itemXml
				.nodeList("com.tle.beans.item.Item/attachments/com.tle.beans.item.attachments.CustomAttachment[value1=\"kaltura\"]");

			// Add server uuid to each attachment xml
			for( Node att : kalturaAttachments )
			{
				String fPath = file.getAbsolutePath();
				String ksPath = fPath.substring(7, fPath.length() - 6);
				SubTemporaryFile ksFolder = new SubTemporaryFile(new ImportFile(ksPath), "kalturaserver");
				List<String> servers = xmlHelper.getXmlFileList(ksFolder);

				if( !Check.isEmpty(servers) )
				{
					String serverPath = servers.get(0);
					KalturaServer ks = (KalturaServer) xmlHelper.readXmlFile(ksFolder, serverPath);

					// Create entry in data map
					Node data = itemXml.node("data", att);

					// add attribute
					Node entry = itemXml.createNode(data, "entry");
					Node key = itemXml.createNode(entry, "string");
					Node value = itemXml.createNode(entry, "string");

					key.setTextContent(KalturaUtils.PROPERTY_KALTURA_SERVER);
					value.setTextContent(ks.getUuid());

					// remove attribute
					itemXml.deleteAll("entry[string=\"dataUrl\"]", data);

					modified = true;
				}
			}
		}

		if( modified )
		{
			xml.setXML(itemXml.toString());
		}

		return modified;
	}
}
