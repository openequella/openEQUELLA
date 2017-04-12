package com.tle.web.kaltura.viewer;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.kaltura.KalturaUtils;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.equella.viewers.AbstractResourceViewer;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.standard.ComponentFactory;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.LinkTagRenderer;
import com.tle.web.viewurl.ResourceViewerConfig;
import com.tle.web.viewurl.ResourceViewerConfigDialog;
import com.tle.web.viewurl.ViewableResource;

@Bind
@Singleton
@SuppressWarnings("nls")
public class KalturaViewer extends AbstractResourceViewer
{
	static
	{
		PluginResourceHandler.init(KalturaViewer.class);
	}

	@PlugURL("css/kalturaviewer.css")
	private static String KALTURA_CSS_URL;
	private static final CssInclude CSS = CssInclude.include(KALTURA_CSS_URL).make();

	@Inject
	private ComponentFactory componentFactory;

	@Override
	public boolean supports(SectionInfo info, ViewableResource resource)
	{
		return KalturaUtils.MIME_TYPE.equals(resource.getMimeType());
	}

	@Override
	public String getViewerId()
	{
		return "kalturaViewer";
	}

	@Override
	protected LinkTagRenderer createLinkFromConfig(SectionInfo info, ViewableResource resource,
		ResourceViewerConfig config, HtmlLinkState state)
	{
		LinkTagRenderer linkTagRenderer = super.createLinkFromConfig(info, resource, config, state);
		linkTagRenderer.getLinkState().addPreRenderable(CSS);
		return linkTagRenderer;
	}

	@Override
	public Class<? extends SectionId> getViewerSectionClass()
	{
		return KalturaViewerSection.class;
	}

	@Override
	public ResourceViewerConfigDialog createConfigDialog(String parentId, SectionTree tree,
		ResourceViewerConfigDialog defaultDialog)
	{
		KalturaViewerConfigDialog cd = componentFactory.createComponent(parentId, "kalturacd", tree,
			KalturaViewerConfigDialog.class, true);
		cd.setTemplate(dialogTemplate);
		return cd;
	}
}
