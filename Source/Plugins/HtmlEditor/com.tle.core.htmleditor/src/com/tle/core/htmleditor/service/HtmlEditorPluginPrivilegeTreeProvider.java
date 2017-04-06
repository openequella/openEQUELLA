package com.tle.core.htmleditor.service;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.htmleditor.beans.HtmlEditorPlugin;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.guice.Bind;
import com.tle.core.security.AbstractEntityPrivilegeTreeProvider;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

@Bind
@Singleton
@SuppressWarnings("nls")
public class HtmlEditorPluginPrivilegeTreeProvider extends AbstractEntityPrivilegeTreeProvider<HtmlEditorPlugin>
{
	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(HtmlEditorPluginPrivilegeTreeProvider.class);

	@Inject
	public HtmlEditorPluginPrivilegeTreeProvider(HtmlEditorPluginService htmleditorService)
	{
		super(htmleditorService, Node.ALL_HTMLEDITOR_PLUGINS, resources.key("securitytree.allhtmleditorplugin"),
			Node.HTMLEDITOR_PLUGIN, resources.key("securitytree.htmleditorplugin"));
	}

	@Override
	protected HtmlEditorPlugin createEntity()
	{
		return new HtmlEditorPlugin();
	}
}
