/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.sections.equella.debug;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.java.plugin.PluginManager;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.IntegrityCheckReport;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.registry.PluginPrerequisite;
import org.java.plugin.registry.PluginRegistry;

import com.tle.core.guice.Bind;
import com.tle.core.plugins.impl.PluginServiceImpl;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.generic.NumberOrder;
import com.tle.web.sections.generic.NumberOrderComparator;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.template.Decorations;

@Bind
public class PluginReport extends AbstractPrototypeSection<PluginReport.Model> implements HtmlRenderer
{
	@ViewFactory
	private FreemarkerFactory viewFactory;
	@Inject
	private PluginServiceImpl pluginService;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		Decorations.getDecorations(context).setTitle(new TextLabel("Plugin Report"));

		PluginManager manager = pluginService.getPluginManager();
		PluginRegistry registry = manager.getRegistry();
		IntegrityCheckReport integrityCheckReport = registry.checkIntegrity(manager.getPathResolver(), true);
		Model model = getModel(context);
		model.setReport(integrityCheckReport);
		Collection<PluginDescriptor> plugins = registry.getPluginDescriptors();
		List<DepRow> depRows = new ArrayList<DepRow>();
		List<DepRow> extRows = new ArrayList<DepRow>();
		int loadedPlugins = 0;
		for( PluginDescriptor desc : plugins )
		{
			boolean pluginActivated = manager.isPluginActivated(desc);
			depRows.add(new DepRow(desc.getId(), countLoadedPlugins(desc, registry, new HashSet<String>()),
				pluginActivated));
			if( pluginActivated )
			{
				loadedPlugins++;
			}
		}
		Collections.sort(depRows, NumberOrderComparator.HIGHEST_FIRST);

		for( PluginDescriptor desc : plugins )
		{
			for( ExtensionPoint extPoint : desc.getExtensionPoints() )
			{
				extRows.add(new DepRow(extPoint.getUniqueId(), countExtensionPlugins(extPoint, registry), false));
			}
		}
		Collections.sort(extRows, NumberOrderComparator.HIGHEST_FIRST);

		model.setExtensionRows(extRows);
		model.setLoadedPlugins(loadedPlugins);
		model.setDependencyRows(depRows);
		return viewFactory.createResult("debug/plugins.ftl", this); //$NON-NLS-1$
	}

	private int countExtensionPlugins(ExtensionPoint point, PluginRegistry registry)
	{
		Collection<Extension> ext = point.getConnectedExtensions();
		Set<String> alreadyLoaded = new HashSet<String>();
		int count = 0;
		for( Extension extension : ext )
		{
			PluginDescriptor desc = extension.getDeclaringPluginDescriptor();
			count += countLoadedPlugins(desc, registry, alreadyLoaded);
		}
		return count;
	}

	private int countLoadedPlugins(PluginDescriptor descriptor, PluginRegistry registry, Set<String> alreadyLoaded)
	{
		if( alreadyLoaded.contains(descriptor.getId()) )
		{
			return 0;
		}
		alreadyLoaded.add(descriptor.getId());
		int count = 1;
		Collection<PluginPrerequisite> preList = descriptor.getPrerequisites();
		for( PluginPrerequisite pre : preList )
		{
			String pluginId = pre.getPluginId();
			PluginDescriptor subDesc = registry.getPluginDescriptor(pluginId);
			count += countLoadedPlugins(subDesc, registry, alreadyLoaded);

		}
		return count;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model();
	}

	public static class Model
	{
		private IntegrityCheckReport report;
		private List<DepRow> dependencyRows;
		private List<DepRow> extensionRows;
		private int loadedPlugins;

		public IntegrityCheckReport getReport()
		{
			return report;
		}

		public void setReport(IntegrityCheckReport report)
		{
			this.report = report;
		}

		public List<DepRow> getDependencyRows()
		{
			return dependencyRows;
		}

		public void setDependencyRows(List<DepRow> dependencyRows)
		{
			this.dependencyRows = dependencyRows;
		}

		public int getLoadedPlugins()
		{
			return loadedPlugins;
		}

		public void setLoadedPlugins(int loadedPlugins)
		{
			this.loadedPlugins = loadedPlugins;
		}

		public List<DepRow> getExtensionRows()
		{
			return extensionRows;
		}

		public void setExtensionRows(List<DepRow> extensionRows)
		{
			this.extensionRows = extensionRows;
		}
	}

	public static class DepRow implements NumberOrder
	{
		private final String name;
		private final int count;
		private final boolean loaded;

		public DepRow(String name, int count, boolean loaded)
		{
			this.name = name;
			this.count = count;
			this.loaded = loaded;
		}

		public String getName()
		{
			return name;
		}

		public int getCount()
		{
			return count;
		}

		@Override
		public int getOrder()
		{
			return count;
		}

		public boolean isLoaded()
		{
			return loaded;
		}
	}
}
