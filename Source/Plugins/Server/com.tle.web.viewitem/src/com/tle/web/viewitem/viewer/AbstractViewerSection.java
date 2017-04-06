package com.tle.web.viewitem.viewer;

import com.tle.annotation.Nullable;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.mime.MimeEntry;
import com.tle.core.mimetypes.MimeTypeConstants;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.viewurl.ResourceViewerConfig;
import com.tle.web.viewurl.ViewAuditEntry;
import com.tle.web.viewurl.ViewItemResource;
import com.tle.web.viewurl.ViewItemViewer;
import com.tle.web.viewurl.ViewableResource;

/**
 * Important to note that the ResourceViewer and the Section are different
 * objects. The viewer looks up the section and executes it.
 * 
 * @author jmaginnis
 */
public abstract class AbstractViewerSection<M> extends AbstractPrototypeSection<M> implements ViewItemViewer
{
	@ViewFactory(fixed = false)
	protected FreemarkerFactory viewFactory;

	@Override
	public ViewAuditEntry getAuditEntry(SectionInfo info, ViewItemResource resource)
	{
		return resource.getViewAuditEntry();
	}

	@Nullable
	@Override
	public IAttachment getAttachment(SectionInfo info, ViewItemResource resource)
	{
		final ViewableResource viewableResource = resource.getAttribute(ViewableResource.class);
		if( viewableResource != null )
		{
			return viewableResource.getAttachment();
		}
		return null;
	}

	protected ResourceViewerConfig getResourceViewerConfig(MimeTypeService mimeTypeService, ViewItemResource resource,
		String viewerId)
	{
		MimeEntry entry = mimeTypeService.getEntryForMimeType(resource.getMimeType());
		ResourceViewerConfig config = null;
		if( entry != null )
		{
			config = mimeTypeService.getBeanFromAttribute(entry, MimeTypeConstants.KEY_VIEWER_CONFIG_PREFIX + viewerId,
				ResourceViewerConfig.class);
		}
		return config;
	}
}
