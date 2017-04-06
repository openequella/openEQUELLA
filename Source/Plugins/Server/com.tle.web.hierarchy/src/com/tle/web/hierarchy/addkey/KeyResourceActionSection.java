package com.tle.web.hierarchy.addkey;

import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;

import com.tle.beans.workflow.WorkflowStatus;
import com.tle.core.guice.Bind;
import com.tle.core.security.TLEAclManager;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.viewitem.summary.sidebar.actions.GenericMinorActionWithPageSection;
import com.tle.web.viewurl.ItemSectionInfo;

@Bind
public class KeyResourceActionSection extends GenericMinorActionWithPageSection
{
	@Inject
	private TLEAclManager aclManager;

	@TreeLookup
	private HierarchyTreeSection selectSection;

	@PlugKey("aftercontribution.link")
	private static Label LABEL_KEYRESOURCE;

	@Override
	protected Label getLinkLabel()
	{
		return LABEL_KEYRESOURCE;
	}

	@SuppressWarnings("nls")
	@Override
	protected boolean canView(SectionInfo info, ItemSectionInfo itemInfo, WorkflowStatus status)
	{
		final Set<String> privilege = aclManager.filterNonGrantedPrivileges(Collections
			.singleton("MODIFY_KEY_RESOURCE"));
		return !privilege.isEmpty();
	}

	@Override
	protected SectionId getPageSection()
	{
		return selectSection;
	}

	@Override
	public String getLinkText()
	{
		return LABEL_KEYRESOURCE.getText();
	}
}
