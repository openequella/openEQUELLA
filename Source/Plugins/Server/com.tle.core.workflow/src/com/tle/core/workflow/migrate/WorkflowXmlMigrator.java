package com.tle.core.workflow.migrate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.thoughtworks.xstream.XStream;
import com.tle.common.workflow.Workflow;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.migration.XmlMigrator;
import com.tle.core.workflow.migrate.beans.node.WorkflowNode;
import com.tle.core.xstream.XmlService;

@Bind
@Singleton
@SuppressWarnings("nls")
public class WorkflowXmlMigrator extends XmlMigrator
{
	@Inject
	private XmlService xmlService;
	private XStream xstream;

	@PostConstruct
	protected void setupXStream()
	{
		xstream = xmlService.createDefault(getClass().getClassLoader());
		xstream.aliasPackage("com.tle.beans.entity.workflow", "com.tle.common.old.workflow");
		xstream.aliasPackage("com.tle.common.workflow", "com.tle.core.workflow.migrate.beans");
	}

	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
	{
		migrateWorkflow(staging);
		migrateCollectionRef(staging);
	}

	private void migrateCollectionRef(TemporaryFileHandle staging)
	{
		SubTemporaryFile folder = new SubTemporaryFile(staging, "itemdefinition");
		List<String> entries = xmlHelper.getXmlFileList(folder);
		for( String entry : entries )
		{
			PropBagEx collection = xmlHelper.readToPropBagEx(folder, entry);
			if( collection.nodeExists("workflow") )
			{
				collection.setNode("workflow/@entityclass", Workflow.class.getName());
				xmlHelper.writeFromPropBagEx(folder, entry, collection);
			}
		}
	}

	private void migrateWorkflow(TemporaryFileHandle staging)
	{
		MigrateWorkflow migrate = new MigrateWorkflow();
		SubTemporaryFile folder = new SubTemporaryFile(staging, "workflow");
		List<String> entries = xmlHelper.getXmlFileList(folder);
		for( String entry : entries )
		{
			PropBagEx workflow = xmlHelper.readToPropBagEx(folder, entry);
			com.tle.common.old.workflow.Workflow oldWorkflow = (com.tle.common.old.workflow.Workflow) xstream
				.fromXML(workflow.toString());
			workflow.setNodeName(Workflow.class.getName());
			workflow.deleteNode("root");
			workflow.deleteNode("allGroups");
			List<com.tle.core.workflow.migrate.beans.node.WorkflowNode> newNodes = migrate.convertNodes(oldWorkflow
				.getRoot());
			workflow.appendChildren("", new PropBagEx(xstream.toXML(new NodeHolder(newNodes))));
			xmlHelper.writeFromPropBagEx(folder, entry, workflow);
		}
	}

	public static class NodeHolder
	{
		private final Set<WorkflowNode> nodes;

		public NodeHolder(List<WorkflowNode> newNodes)
		{
			nodes = new HashSet<WorkflowNode>(newNodes);
		}

		public Set<WorkflowNode> getNodes()
		{
			return nodes;
		}
	}
}
