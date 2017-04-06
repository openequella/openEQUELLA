package com.tle.web.viewitem.viewer;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.standard.ComponentFactory;
import com.tle.web.stream.ContentStreamWriter;
import com.tle.web.viewurl.ResourceViewerConfigDialog;
import com.tle.web.viewurl.ViewAuditEntry;
import com.tle.web.viewurl.ViewItemResource;
import com.tle.web.viewurl.ViewItemViewer;
import com.tle.web.viewurl.ViewableResource;

@Bind
@Singleton
@SuppressWarnings("nls")
public class DownloadFileViewer extends FileViewer implements ViewItemViewer
{
	@Inject
	private ContentStreamWriter contentStreamWriter;
	@Inject
	private ComponentFactory componentFactory;

	@Override
	public String getViewerId()
	{
		return "save";
	}

	@Override
	public ResourceViewerConfigDialog createConfigDialog(String parentId, SectionTree tree,
		ResourceViewerConfigDialog defaultDialog)
	{
		DownloadFileViewerConfigDialog cd = componentFactory.createComponent(parentId, "dfcd", tree,
			DownloadFileViewerConfigDialog.class, true);
		cd.setTemplate(dialogTemplate);
		return cd;
	}

	@Override
	public Bookmark createStreamUrl(SectionInfo info, ViewableResource resource)
	{
		return super.createViewItemUrl(info, resource);
	}

	@Override
	public ViewItemViewer getViewer(SectionInfo info, ViewItemResource resource)
	{
		return this;
	}

	@Override
	public ViewAuditEntry getAuditEntry(SectionInfo info, ViewItemResource resource)
	{
		return null;
	}

	@Override
	public Collection<String> ensureOnePrivilege()
	{
		return VIEW_ITEM_AND_VIEW_ATTACHMENTS_PRIV;
	}

	@Override
	public SectionResult view(RenderContext info, ViewItemResource resource)
	{
		info.setRendered();
		contentStreamWriter.outputStream(info.getRequest(), info.getResponse(),
			new AttachmentContentStream(resource.getContentStream()));
		return null;
	}

	@Override
	public boolean supports(SectionInfo info, ViewableResource resource)
	{
		String mimeType = resource.getMimeType();
		return mimeType != null && !mimeType.toLowerCase().startsWith("equella/");
	}
}
