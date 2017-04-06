package com.tle.web.htmleditor.tinymce.addon.tle;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.Lists;
import com.tle.core.guice.Bind;
import com.tle.web.htmleditor.tinymce.TinyMceAddOn;
import com.tle.web.htmleditor.tinymce.TinyMceAddonProvider;

/**
 * @author Aaron
 */
@Bind
@Singleton
public class InbuiltTinyMceAddonProvider implements TinyMceAddonProvider
{
	@Inject
	private TleFileUploader fileUploader;
	@Inject
	private TleResourceLinker resourceLinker;
	@Inject
	private TleScrapbookEmbedder scrapbookEmbedder;

	@Override
	public List<TinyMceAddOn> getAddons()
	{
		return Lists.newArrayList((TinyMceAddOn) fileUploader, resourceLinker, scrapbookEmbedder);
	}
}
