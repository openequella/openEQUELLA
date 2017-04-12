package com.tle.web.searching;

import com.tle.beans.item.attachments.Attachment;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.PreRenderable;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.viewable.ViewableItem;

/**
 * @author Peng
 */
public interface VideoPreviewRenderer extends PreRenderable
{
	SectionRenderable renderPreview(RenderContext context, Attachment attachment, ViewableItem<?> vitem, String mimeType);

	boolean supports(String mimeType);
}
