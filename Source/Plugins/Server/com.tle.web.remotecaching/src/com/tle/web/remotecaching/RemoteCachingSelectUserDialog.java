package com.tle.web.remotecaching;

import java.util.Set;

import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.utils.SelectUserDialog;

/**
 * @author Aaron
 */
@Bind
public class RemoteCachingSelectUserDialog extends SelectUserDialog
{
	@Override
	public void registered(String id, SectionTree tree)
	{
		setMultipleUsers(true);
		super.registered(id, tree);
	}

	public void setUserExclusions(SectionInfo info, Set<String> userExclusions)
	{
		section.setUserExclusions(info, userExclusions);
	}
}
