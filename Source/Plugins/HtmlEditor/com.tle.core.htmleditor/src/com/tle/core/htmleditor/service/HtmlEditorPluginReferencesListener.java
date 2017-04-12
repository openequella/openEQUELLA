package com.tle.core.htmleditor.service;

import java.util.List;

import com.tle.common.htmleditor.beans.HtmlEditorPlugin;
import com.tle.core.events.listeners.ApplicationListener;

/**
 * @author Aaron
 */
public interface HtmlEditorPluginReferencesListener extends ApplicationListener
{
	void addHtmlEditorPluginReferencingClasses(HtmlEditorPlugin client, List<Class<?>> referencingClasses);
}
