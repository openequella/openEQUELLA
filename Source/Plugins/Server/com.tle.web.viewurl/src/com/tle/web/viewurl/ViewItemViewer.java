package com.tle.web.viewurl;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderContext;

@NonNullByDefault
@SuppressWarnings("nls")
public interface ViewItemViewer
{
	Collection<String> VIEW_ITEM_AND_VIEW_ATTACHMENTS_PRIV = Arrays.asList("VIEW_ITEM", "VIEW_ATTACHMENTS");
	Collection<String> DISCOVER_AND_VIEW_PRIVS = Arrays.asList("DISCOVER_ITEM", "VIEW_ITEM");
	Collection<String> VIEW_RESTRICTED_ATTACHMENTS = Collections.singleton("VIEW_RESTRICTED_ATTACHMENTS");

	@Nullable
	Collection<String> ensureOnePrivilege();

	/**
	 * You might return null if you doing a forward
	 * 
	 * @param info
	 * @param resource
	 * @return
	 * @throws IOException
	 */
	@Nullable
	SectionResult view(RenderContext info, ViewItemResource resource) throws IOException;

	@Nullable
	ViewAuditEntry getAuditEntry(SectionInfo info, ViewItemResource resource);

	@Nullable
	IAttachment getAttachment(SectionInfo info, ViewItemResource resource);
}
