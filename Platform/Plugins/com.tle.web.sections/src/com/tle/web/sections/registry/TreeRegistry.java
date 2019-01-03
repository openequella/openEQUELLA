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

package com.tle.web.sections.registry;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;

import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.tle.core.guice.Bind;
import com.tle.core.guice.Bindings;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.web.sections.RegistrationController;
import com.tle.web.sections.RegistrationHandler;
import com.tle.web.sections.Section;
import com.tle.web.sections.SectionNode;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.generic.DefaultSectionTree;
import com.tle.web.sections.registry.handler.BookmarkRegistrationHandler;
import com.tle.web.sections.registry.handler.EventFactoryHandler;
import com.tle.web.sections.registry.handler.StandardInterfaces;
import com.tle.web.sections.registry.handler.TreeLookupRegistrationHandler;

/**
 * Responsible for mapping URLs to a {@link SectionTree}.
 * <p>
 * It also doubles up as the {@link RegistrationController}, so contains the
 * logic for calling {@link RegistrationHandler}s.
 * <p>
 * This implementation has a list of default {@code RegistrationHandler}s, and
 * uses JPF to get any additional handlers. <br>
 * The default handler list contains:
 * <ul>
 * <li>{@link com.tle.web.sections.registry.handler.StandardInterfaces}</li>
 * <li>{@link com.tle.web.sections.registry.handler.BookmarkRegistrationHandler}
 * </li>
 * <li>{@link com.tle.web.sections.registry.handler.EventFactoryHandler}</li>
 * <li>
 * {@link com.tle.web.sections.registry.handler.TreeLookupRegistrationHandler}</li>
 * </ul>
 * 
 * @author jmaginnis
 */
@Bindings({@Bind(RegistrationController.class), @Bind(TreeRegistry.class)})
@Singleton
@SuppressWarnings("nls")
public class TreeRegistry implements RegistrationController
{
	private PluginService pluginService;
	private PluginTracker<Object> sectionTreePlugin;
	private PluginTracker<Object> sectionPlugin;
	private PluginTracker<RegistrationHandler> registrationPlugins;
	private final LoadingCache<String, SectionTreeData> treeMap = CacheBuilder.newBuilder().build(
		CacheLoader.from(new TreeMaker()));
	private List<RegistrationHandler> handlers;

	@Inject
	private StandardInterfaces standardHandler;
	@Inject
	private BookmarkRegistrationHandler bookmarkHandler;
	@Inject
	private EventFactoryHandler eventHandler;
	@Inject
	private TreeLookupRegistrationHandler treeLookupHandler;

	@PostConstruct
	public void setupHandlers()
	{
		this.handlers = ImmutableList.of(standardHandler, bookmarkHandler, eventHandler, treeLookupHandler);
	}

	public SectionTree getTreeForPath(String path)
	{
		return getTreeForPath(path, false);
	}

	public class TreeMaker implements Function<String, SectionTreeData>
	{

		@Override
		public SectionTreeData apply(String path)
		{
			SectionTreeData data = new SectionTreeData();
			Map<String, Extension> extMap = sectionTreePlugin.getExtensionMap();
			Extension treeExtension = extMap.get(path);
			if( treeExtension == null )
			{
				return data;
			}
			Parameter param = treeExtension.getParameter("url");
			boolean url = param == null || param.valueAsBoolean();
			DefaultSectionTree sectionTree = createTree(treeExtension);
			Collection<Extension> extensions = sectionPlugin.getExtensions();
			for( Extension extension : extensions )
			{
				String sectPath = extension.getParameter("path").valueAsString();
				if( sectPath.equals(path) || (sectPath + ".do").equals(path) )
				{
					boolean after = false;

					final Parameter afterParam = extension.getParameter("afterid");
					final Parameter beforeParam = extension.getParameter("beforeid");
					final Parameter layoutParam = extension.getParameter("layout");

					String insertId = null;
					if( afterParam != null )
					{
						after = true;
						insertId = afterParam.valueAsString();
					}
					else if( beforeParam != null )
					{
						insertId = beforeParam.valueAsString();
					}
					insertId = sectionTree.getPlaceHolder(insertId);

					String parentId = extension.getParameter("parentid").valueAsString();
					parentId = sectionTree.getPlaceHolder(parentId);

					for( Parameter classParam : extension.getParameters("class") )
					{
						final Object section = pluginService.getBean(extension.getDeclaringPluginDescriptor(),
							classParam.valueAsString());
						final String regId = sectionTree.registerSections(section, parentId, insertId, after);
						if( layoutParam != null )
						{
							sectionTree.setLayout(regId, layoutParam.valueAsString());
						}
					}
				}
			}
			treeFinished(sectionTree);
			data.tree = sectionTree;
			data.url = url;
			return data;
		}

	}

	public SectionTree getTreeForPath(String path, boolean fromUrl)
	{
		SectionTreeData treeData = treeMap.getUnchecked(path);
		if( treeData.tree == null || (fromUrl && !treeData.url) )
		{
			return null;
		}
		return treeData.tree;
	}

	private DefaultSectionTree createTree(Extension treeExtension)
	{
		SectionNode rootNode;
		Object rootObj = sectionTreePlugin.getBeanByParameter(treeExtension, "root"); //$NON-NLS-1$
		if( rootObj instanceof Section )
		{
			rootNode = new SectionNode();
			rootNode.setSection((Section) rootObj);
		}
		else
		{
			rootNode = (SectionNode) rootObj;
		}
		DefaultSectionTree sectionTree = new DefaultSectionTree(this, rootNode);
		Collection<Parameter> attributes = treeExtension.getParameters("attribute"); //$NON-NLS-1$
		for( Parameter attrParam : attributes )
		{
			Object key;
			Object value;
			Parameter keyParam = attrParam.getSubParameter("key"); //$NON-NLS-1$
			if( keyParam == null )
			{
				keyParam = attrParam.getSubParameter("keyClass"); //$NON-NLS-1$
				if( keyParam == null )
				{
					throw new SectionsRuntimeException("Must use one of 'key', or 'keyClass'"); //$NON-NLS-1$
				}
				key = sectionTreePlugin.getClassForName(treeExtension, keyParam.valueAsString());
			}
			else
			{
				key = keyParam.valueAsString();
			}
			Parameter valueParam = attrParam.getSubParameter("value"); //$NON-NLS-1$
			if( valueParam == null )
			{
				valueParam = attrParam.getSubParameter("bean"); //$NON-NLS-1$
				if( valueParam == null )
				{
					throw new SectionsRuntimeException("Must use one of 'value' or 'bean'"); //$NON-NLS-1$
				}
				value = sectionTreePlugin.getBeanByParameter(treeExtension, valueParam);
			}
			else
			{
				value = valueParam.valueAsString();
			}
			sectionTree.setAttribute(key, value);
		}
		return sectionTree;
	}

	@Override
	public void treeFinished(SectionTree tree)
	{
		tree.runDelayedRegistration();
		List<RegistrationHandler> extras = tree.getExtraRegistrationHandlers();
		for( RegistrationHandler handler : handlers )
		{
			handler.treeFinished(tree);
		}
		List<RegistrationHandler> pluginHandlers = registrationPlugins.getBeanList();
		for( RegistrationHandler handler : pluginHandlers )
		{
			handler.treeFinished(tree);
		}
		for( RegistrationHandler handler : extras )
		{
			handler.treeFinished(tree);
		}
		tree.finished();
	}

	@Override
	public void registered(String id, SectionTree tree, Section section)
	{
		List<RegistrationHandler> extras = tree.getExtraRegistrationHandlers();
		for( RegistrationHandler handler : handlers )
		{
			handler.registered(id, tree, section);
		}
		List<RegistrationHandler> pluginHandlers = registrationPlugins.getBeanList();
		for( RegistrationHandler handler : pluginHandlers )
		{
			handler.registered(id, tree, section);
		}
		for( RegistrationHandler handler : extras )
		{
			handler.registered(id, tree, section);
		}
	}

	private static class SectionTreeData
	{
		SectionTree tree;
		boolean url;
	}

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		this.pluginService = pluginService;
		sectionTreePlugin = new PluginTracker<Object>(pluginService, "com.tle.web.sections", "sectionTree", "path");
		sectionPlugin = new PluginTracker<Object>(pluginService, "com.tle.web.sections", "section", null,
				new PluginTracker.ExtensionParamComparator("order"));
		registrationPlugins = new PluginTracker<RegistrationHandler>(pluginService, "com.tle.web.sections",
			"registrationHandler", null, new PluginTracker.ExtensionParamComparator("order"));
		registrationPlugins.setBeanKey("class");
	}

	public void clearAll()
	{
		treeMap.invalidateAll();
	}
}
