package com.tle.core.htmleditor.service;

import java.util.List;

import com.google.common.collect.Lists;
import com.tle.common.htmleditor.beans.HtmlEditorPlugin;
import com.tle.core.events.BaseEntityReferencesEvent;

public class HtmlEditorPluginReferencesEvent extends BaseEntityReferencesEvent<HtmlEditorPlugin, HtmlEditorPluginReferencesListener>
{
	private static final long serialVersionUID = 1L;

	private final List<Class<?>> referencingClasses = Lists.newArrayList();

	public HtmlEditorPluginReferencesEvent(HtmlEditorPlugin client)
	{
		super(client);
	}

	@Override
	public Class<HtmlEditorPluginReferencesListener> getListener()
	{
		return HtmlEditorPluginReferencesListener.class;
	}

	@Override
	public void postEvent(HtmlEditorPluginReferencesListener listener)
	{
		listener.addHtmlEditorPluginReferencingClasses(entity, referencingClasses);
	}

	public List<Class<?>> getReferencingClasses()
	{
		return referencingClasses;
	}
}
