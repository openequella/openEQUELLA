package com.tle.web.viewitem.section;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.tle.annotation.Nullable;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.Attachments;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewitem.section.PathMapper.Type;
import com.tle.web.viewurl.ViewAuditEntry;
import com.tle.web.viewurl.ViewItemResource;
import com.tle.web.viewurl.ViewItemViewer;

public class ViewDefaultSection extends AbstractPrototypeSection<Object> implements ViewItemViewer
{
	@TreeLookup
	private ViewAttachmentSection attachmentSection;
	@TreeLookup
	private RootItemFileSection rootSection;

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		rootSection.addViewerMapping(Type.FULL, this, "viewdefault.jsp"); //$NON-NLS-1$
	}

	@Override
	public SectionResult view(RenderContext info, ViewItemResource resource)
	{
		final IAttachment attachment = getAttachment(info, resource);
		if( attachment != null )
		{
			attachmentSection.setAttachmentToView(info, attachment.getUuid());
		}

		final ViewableItem<?> viewableItem = resource.getViewableItem();
		final URI path = viewableItem.getServletPath();
		info.setAttribute(SectionInfo.KEY_PATH, path.getPath());
		info.forwardToUrl(info.getPublicBookmark().getHref());
		return null;
	}

	@Override
	public Collection<String> ensureOnePrivilege()
	{
		// We're redirecting to the viewer for whichever attachment is the
		// "default", so don't bother checking privs here - it will be done by
		// the other viewer.
		return null;
	}

	@Override
	public ViewAuditEntry getAuditEntry(SectionInfo info, ViewItemResource resource)
	{
		return null;
	}

	@Nullable
	@Override
	public IAttachment getAttachment(SectionInfo info, ViewItemResource resource)
	{
		final ViewableItem<?> viewableItem = resource.getViewableItem();
		final Attachments attachments = new UnmodifiableAttachments(viewableItem.getItem());
		final List<IAttachment> attachList = new ArrayList<IAttachment>();
		attachList.addAll(attachments.getList(AttachmentType.LINK));
		attachList.addAll(attachments.getList(AttachmentType.IMS));
		attachList.addAll(attachments.getList(AttachmentType.FILE));
		return (attachList.size() == 1 ? attachList.get(0) : null);
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "viewdefault"; //$NON-NLS-1$
	}

	@Override
	public Class<Object> getModelClass()
	{
		return Object.class;
	}

}
