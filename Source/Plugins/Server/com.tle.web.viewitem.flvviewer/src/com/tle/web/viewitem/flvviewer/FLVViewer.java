package com.tle.web.viewitem.flvviewer;

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
public class FLVViewer extends AbstractResourceViewer
{
	@Inject
	private ComponentFactory componentFactory;

	@Override
	public Class<? extends SectionId> getViewerSectionClass()
	{
		return FLVViewerSection.class;
	}

	@Override
	public boolean supports(SectionInfo info, ViewableResource resource)
	{
		return "video/x-flv".equals(resource.getMimeType()); //$NON-NLS-1$
	}

	@Override
	public String getViewerId()
	{
		return "flvViewer"; //$NON-NLS-1$
	}

	@Override
	public ResourceViewerConfigDialog createConfigDialog(String parentId, SectionTree tree,
		ResourceViewerConfigDialog defaultDialog)
	{
		FLVViewerConfigDialog cd = componentFactory.createComponent(parentId, "flvcd", tree, //$NON-NLS-1$
			FLVViewerConfigDialog.class, true);
		cd.setTemplate(dialogTemplate);
		return cd;
	}
}
