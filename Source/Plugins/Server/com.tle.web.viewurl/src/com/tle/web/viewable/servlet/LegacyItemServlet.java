package com.tle.web.viewable.servlet;

import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.web.viewable.NewDefaultViewableItem;
import com.tle.web.viewable.NewViewableItemState;
import com.tle.web.viewable.ViewableItem;

@Bind
@Singleton
public class LegacyItemServlet extends ItemServlet
{
	@Override
	protected ItemUrlParser getItemUrlParser()
	{
		return new NewItemUrlParser()
		{
			@Override
			protected void setupContext()
			{
				String itemdef = partList.get(0);
				context = request.getServletPath().substring(1) + '/' + itemdef + '/';
				partList = partList.subList(1, partList.size());
			}

			@Override
			public ViewableItem createViewableItem()
			{
				NewDefaultViewableItem viewableItem = (NewDefaultViewableItem) super.createViewableItem();
				NewViewableItemState state = viewableItem.getState();
				state.setContext(context);
				return viewableItem;
			}

		};
	}
}
