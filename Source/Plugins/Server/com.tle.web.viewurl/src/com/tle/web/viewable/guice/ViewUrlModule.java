package com.tle.web.viewable.guice;

import com.google.inject.util.Types;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.core.guice.PluginTrackerModule;
import com.tle.web.viewable.ViewableItemResolverExtension;
import com.tle.web.viewurl.attachments.AttachmentResourceExtension;
import com.tle.web.viewurl.attachments.AttachmentTreeExtension;

public class ViewUrlModule extends PluginTrackerModule
{
	@SuppressWarnings("nls")
	@Override
	protected void configure()
	{
		bindTracker(Types.newParameterizedType(AttachmentResourceExtension.class, IAttachment.class),
			"attachmentResource", "class").orderByParameter("order");
		bindTracker(AttachmentTreeExtension.class, "attachmentTree", "bean");
		bindTracker(ViewableItemResolverExtension.class, "viewableItemResolver", "bean").setIdParam("id");
	}
}
