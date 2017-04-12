package com.tle.core.kaltura.migration;

import java.util.List;

import javax.inject.Singleton;

import org.w3c.dom.Node;

import com.dytech.devlib.PropBagEx;
import com.tle.common.Check;
import com.tle.common.kaltura.admin.control.KalturaSettings;
import com.tle.common.kaltura.entity.KalturaServer;
import com.tle.common.util.XmlDocument;
import com.tle.common.util.XmlDocument.NodeListIterable;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.migration.XmlMigrator;

@Bind
@Singleton
@SuppressWarnings("nls")
public class UpdateKalturaControlsXmlMigration extends XmlMigrator
{
	private static final String KALTURA_HANDLER_XPATH = "attributes/entry/set/string[text()=\"kalturaHandler\"]";
	private static final String UNIVERSAL_CONTROLS_XPATH = "pages/com.dytech.edge.wizard.beans.DefaultWizardPage/controls/com.dytech.edge.wizard.beans.control.CustomControl[classType=\"universal\"]";

	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
	{
		TemporaryFileHandle idefFolder = new SubTemporaryFile(staging, "itemdefinition");
		TemporaryFileHandle ksFolder = new SubTemporaryFile(staging, "kalturaserver");

		for( String entry : xmlHelper.getXmlFileList(idefFolder) )
		{
			final PropBagEx itemDefXml = xmlHelper.readToPropBagEx(idefFolder, entry);
			XmlDocument xmlDoc = new XmlDocument(itemDefXml.toString());
			List<String> servers = xmlHelper.getXmlFileList(ksFolder);

			if( !Check.isEmpty(servers) )
			{
				String serverPath = servers.get(0);
				KalturaServer ks = (KalturaServer) xmlHelper.readXmlFile(ksFolder, serverPath);

				if( ks != null )
				{
					if( addKalturaServerAttributes(xmlDoc,
						xmlDoc.node("com.tle.beans.entity.itemdef.ItemDefinition/slow/wizard"), ks.getUuid()) )
					{
						xmlHelper.writeFile(idefFolder, entry, xmlDoc.toString());
					}
				}
			}
		}
	}

	/**
	 * Loops over each of the Universal attachment controls in the given wizard
	 * XML and if they have a Kaltura handler it adds the appropriate attributes
	 * for the given Kaltura server
	 * 
	 * @param xmlDoc the XML doc (either wizard or itemdef)
	 * @param context location of Wizard node
	 * @param ks KalturaServer
	 * @return success(added attributes)
	 */
	public static boolean addKalturaServerAttributes(XmlDocument xmlDoc, Node context, String ksUuid)
	{
		boolean added = false;
		NodeListIterable universalControls = xmlDoc.nodeList(UNIVERSAL_CONTROLS_XPATH, context);
		for( Node uc : universalControls )
		{
			// Does this control have a kaltura handler?
			if( xmlDoc.node(KALTURA_HANDLER_XPATH, uc) != null )
			{
				// if so add the server attributes to the control
				Node attrs = xmlDoc.node("attributes", uc);

				// add attribute
				Node entry = xmlDoc.createNode(attrs, "entry");
				Node key = xmlDoc.createNode(entry, "string");
				Node value = xmlDoc.createNode(entry, "string");

				key.setTextContent(KalturaSettings.KEY_SERVER_UUID);
				value.setTextContent(ksUuid);
				added = true;
			}
		}
		return added;
	}

}
