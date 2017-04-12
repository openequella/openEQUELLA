package com.tle.core.workflow.migrate;

import java.util.List;

import javax.inject.Singleton;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.NodeList;

import com.dytech.devlib.PropBagEx;
import com.tle.common.workflow.node.WorkflowItem.Priority;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.migration.XmlMigrator;

@Bind
@Singleton
public class AddNotificationSchemaXML extends XmlMigrator
{
	private XPath xpath;
	private XPathExpression allNodes;

	@SuppressWarnings("nls")
	public AddNotificationSchemaXML()
	{
		XPathFactory factory = XPathFactory.newInstance();
		xpath = factory.newXPath();
		try
		{
			allNodes = xpath.compile("nodes/com.tle.common.workflow.node.WorkflowItem");
		}
		catch( XPathExpressionException e )
		{
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("nls")
	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
		throws XPathExpressionException
	{
		SubTemporaryFile folder = new SubTemporaryFile(staging, "workflow");
		List<String> entries = xmlHelper.getXmlFileList(folder);
		for( String entry : entries )
		{
			PropBagEx workflow = xmlHelper.readToPropBagEx(folder, entry);
			NodeList zeroDays = (NodeList) allNodes.evaluate(workflow.getRootElement(), XPathConstants.NODESET);
			for( int i = 0; i < zeroDays.getLength(); i++ )
			{
				PropBagEx bag = new PropBagEx(zeroDays.item(i), true);
				bag.setNode("priority", Priority.NORMAL.intValue());
				if( bag.isNodeTrue("escalate") && bag.getIntNode("escalationdays") == 0 )
				{
					bag.setNode("escalate", "false");
				}
			}
			xmlHelper.writeFromPropBagEx(folder, entry, workflow);
		}
	}
}
