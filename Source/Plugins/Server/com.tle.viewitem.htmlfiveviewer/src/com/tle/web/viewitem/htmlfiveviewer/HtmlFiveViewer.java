package com.tle.web.viewitem.htmlfiveviewer;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.viewers.AbstractResourceViewer;
import com.tle.web.sections.standard.ComponentFactory;
import com.tle.web.viewurl.ResourceViewerConfigDialog;
import com.tle.web.viewurl.ViewableResource;

@Bind
@Singleton
@SuppressWarnings("nls")
public class HtmlFiveViewer extends AbstractResourceViewer
{
	@Inject
	private ComponentFactory componentFactory;

	@Override
	public boolean supports(SectionInfo info, ViewableResource resource)
	{
		String mimeType = resource.getMimeType();
		return mimeType.contains("video")
			&& (mimeType.contains("ogg") || mimeType.contains("mp4") || mimeType.contains("webm"));
	}

	@Override
	public String getViewerId()
	{
		return "htmlFiveViewer";
	}

	@Override
	public Class<? extends SectionId> getViewerSectionClass()
	{

		return HtmlFiveViewerSection.class;
	}

	@Override
	public ResourceViewerConfigDialog createConfigDialog(String parentId, SectionTree tree,
		ResourceViewerConfigDialog defaultDialog)
	{
		HtmlFiveViewerConfigDialog configDialog = componentFactory.createComponent(parentId, "html5cd", tree,
			HtmlFiveViewerConfigDialog.class, true);
		configDialog.setTemplate(dialogTemplate);
		return configDialog;
	}
}
