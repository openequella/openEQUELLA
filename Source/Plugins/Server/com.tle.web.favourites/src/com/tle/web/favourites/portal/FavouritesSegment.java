package com.tle.web.favourites.portal;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.home.RecentSelectionsSegment;

@Bind
public class FavouritesSegment extends FavouritesPortletRenderer implements RecentSelectionsSegment
{
	@Override
	public String getTitle(SectionInfo info, SelectionSession session)
	{
		return CurrentLocale.get("com.tle.web.favourites.portal.name"); //$NON-NLS-1$
	}
}
