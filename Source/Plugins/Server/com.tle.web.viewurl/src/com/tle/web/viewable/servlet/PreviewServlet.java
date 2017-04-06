package com.tle.web.viewable.servlet;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.core.services.user.UserSessionService;
import com.tle.web.viewable.PreviewableItem;
import com.tle.web.viewable.ViewableItem;

@Bind
@Singleton
public class PreviewServlet extends ItemServlet
{
	private static final long serialVersionUID = 1L;

	@Inject
	private UserSessionService sessionService;

	@Override
	protected ItemUrlParser getItemUrlParser()
	{
		return new NewItemUrlParser()
		{
			@Override
			public ViewableItem createViewableItem()
			{
				String uuid = itemId.getUuid();
				PreviewableItem previewableItem = sessionService.getAttribute(uuid);
				if( previewableItem != null )
				{
					ViewableItem viewableItem = previewableItem.getViewableItem();
					viewableItem.setFromRequest(true);
					return viewableItem;
				}
				return null;
			}
		};
	}
}
