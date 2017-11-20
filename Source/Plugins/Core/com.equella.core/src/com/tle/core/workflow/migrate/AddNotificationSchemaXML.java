/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.common.workflow.node.WorkflowItem.Priority;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.convert.XmlMigrator;

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
