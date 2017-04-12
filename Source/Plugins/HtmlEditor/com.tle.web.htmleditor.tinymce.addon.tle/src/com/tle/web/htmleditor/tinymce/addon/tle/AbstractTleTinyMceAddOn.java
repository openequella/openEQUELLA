package com.tle.web.htmleditor.tinymce.addon.tle;

import javax.inject.Inject;

import com.tle.common.PathUtils;
import com.tle.core.services.UrlService;
import com.tle.web.htmleditor.tinymce.TinyMceAddOn;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.sections.SectionTree;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public abstract class AbstractTleTinyMceAddOn implements TinyMceAddOn
{
	@Inject
	private UrlService urlService;

	@Override
	public String getBaseUrl()
	{
		return urlService.institutionalise(getResourceHelper().url("scripts/" + getId()));
	}

	@Override
	public String getJsUrl()
	{
		return PathUtils.urlPath(getBaseUrl(), "editor_plugin_src.js");
	}

	protected abstract PluginResourceHelper getResourceHelper();

	@Override
	public boolean isEnabled()
	{
		return true;
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		// don't register anything by default
	}
}
