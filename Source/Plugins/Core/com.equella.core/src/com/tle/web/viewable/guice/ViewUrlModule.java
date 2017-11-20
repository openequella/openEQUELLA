/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.viewable.guice;

import com.google.inject.util.Types;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.core.guice.PluginTrackerModule;
import com.tle.web.viewable.ViewableItemResolverExtension;
import com.tle.web.viewurl.attachments.AttachmentResourceExtension;
import com.tle.web.viewurl.attachments.AttachmentTreeExtension;

public class ViewUrlModule extends PluginTrackerModule
{

	@Override
	protected String getPluginId()
	{
		return "com.tle.web.viewurl";
	}

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
