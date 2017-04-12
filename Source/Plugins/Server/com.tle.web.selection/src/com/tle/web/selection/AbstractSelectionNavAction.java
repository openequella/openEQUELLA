package com.tle.web.selection;

import java.util.Set;

import com.tle.web.sections.SectionInfo;

/**
 * @author Aaron
 */
public abstract class AbstractSelectionNavAction implements SelectionNavAction
{
	@Override
	public boolean isActionAvailable(SectionInfo info, SelectionSession session)
	{
		final Set<String> navActions = session.getAllowedSelectNavActions();
		if( navActions != null && !navActions.contains(getActionType()) )
		{
			return false;
		}
		return true;
	}
}
