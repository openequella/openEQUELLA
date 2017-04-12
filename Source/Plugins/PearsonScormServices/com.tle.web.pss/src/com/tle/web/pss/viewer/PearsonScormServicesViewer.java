package com.tle.web.pss.viewer;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.MimeTypeConstants;
import com.tle.core.pss.util.PSSConstants;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.viewers.AbstractResourceViewer;
import com.tle.web.sections.standard.ComponentFactory;
import com.tle.web.viewurl.ResourceViewerConfigDialog;
import com.tle.web.viewurl.ViewableResource;

@Bind
@Singleton
public class PearsonScormServicesViewer extends AbstractResourceViewer
{
	@Inject
	private ComponentFactory componentFactory;

	@Override
	public boolean supports(SectionInfo info, ViewableResource resource)
	{
		return resource.getMimeType().equals(MimeTypeConstants.MIME_SCORM);
	}

	@Override
	public String getViewerId()
	{
		return PSSConstants.PSS_VIEWER_ID;
	}

	@Override
	public Class<? extends SectionId> getViewerSectionClass()
	{
		return PearsonScormServicesViewerSection.class;
	}

	@Override
	public ResourceViewerConfigDialog createConfigDialog(String parentId, SectionTree tree,
		ResourceViewerConfigDialog defaultDialog)
	{
		PearsonScormServicesViewerConfigDialog cd = componentFactory.createComponent(parentId, "pssvcd", tree,
			PearsonScormServicesViewerConfigDialog.class, true);
		cd.setTemplate(dialogTemplate);
		return cd;
	}
}
