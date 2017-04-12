package com.tle.core.htmleditor.service;

import java.io.InputStream;
import java.util.List;

import com.tle.common.htmleditor.beans.HtmlEditorPlugin;
import com.tle.core.services.entity.AbstractEntityService;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public interface HtmlEditorPluginService extends AbstractEntityService<HtmlEditorPluginEditingBean, HtmlEditorPlugin>
{
	String ENTITY_TYPE = "HTMLEDITOR_PLUGIN";

	HtmlEditorPlugin getByPluginId(String pluginId);

	/**
	 * Closes the zipStream when it's done
	 * 
	 * @param zipStream
	 * @throws InvalidHtmlEditorPluginException
	 */
	void uploadPlugin(InputStream zipStream) throws InvalidHtmlEditorPluginException;

	List<HtmlEditorPlugin> enumerateForType(String type);
}
