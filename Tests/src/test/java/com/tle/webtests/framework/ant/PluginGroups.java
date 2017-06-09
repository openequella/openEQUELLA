package com.tle.webtests.framework.ant;

import java.io.BufferedWriter;
import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.registry.PluginRegistry;
import org.java.plugin.tools.ant.BaseJpfTask;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@SuppressWarnings("nls")
public class PluginGroups extends BaseJpfTask
{

	@Override
	public void execute() throws BuildException
	{
		initRegistry(true);
		try
		{
			Gson gson = new GsonBuilder().setPrettyPrinting()
				.registerTypeAdapter(PluginNode.class, new PluginNodeDeserializer()).create();

			File groupingsFile = new File("groupings.json");
			PluginNode rootNode = gson.fromJson(Files.newReader(groupingsFile, Charsets.UTF_8), PluginNode.class);

			File backupFile = new File("groupings.json.bak");
			groupingsFile.renameTo(backupFile);
			try
			{
				BufferedWriter writer = Files.newWriter(groupingsFile, Charsets.UTF_8);
				gson.toJson(rootNode, writer);
				writer.close();
				backupFile.delete();
			}
			catch( Exception e )
			{
				backupFile.renameTo(groupingsFile);
				throw new RuntimeException(e);
			}

			PluginRegistry registry = getRegistry();
			Set<String> allPluginIds = Sets.newHashSet(Collections2.transform(registry.getPluginDescriptors(),
				new Function<PluginDescriptor, String>()
				{
					@Override
					public String apply(PluginDescriptor input)
					{
						return input.getId();
					}
				}));

			LinkedList<PluginNode> nodesToCheck = new LinkedList<PluginNode>();
			nodesToCheck.add(rootNode);
			while( !nodesToCheck.isEmpty() )
			{
				PluginNode node = nodesToCheck.removeFirst();
				List<String> plugins = node.getPlugins();
				if( plugins != null )
				{
					allPluginIds.removeAll(plugins);
				}
				nodesToCheck.addAll(node.getChildren());
			}
			List<String> orderedPlugins = Lists.newArrayList(allPluginIds);
			Collections.sort(orderedPlugins);
			for( String id : orderedPlugins )
			{
				System.err.println('"' + id + "\",");
			}
			System.err.println(orderedPlugins.size());
		}
		catch( Exception e )
		{
			throw new BuildException(e);
		}
	}

	public static void main(String[] args)
	{
		PluginGroups grouping = new PluginGroups();
		Project project = new Project();
		grouping.setProject(project);
		grouping.setBaseDir(new File("/home/jolz/gitrepos/trunk/Source/Plugins")); //$NON-NLS-1$
		grouping.setIncludes("**/plugin-jpf.xml"); //$NON-NLS-1$
		DefaultLogger consoleLogger = new DefaultLogger();
		consoleLogger.setErrorPrintStream(System.err);
		consoleLogger.setOutputPrintStream(System.out);
		consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
		project.addBuildListener(consoleLogger);
		grouping.execute();
	}
}
