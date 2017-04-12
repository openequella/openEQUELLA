package com.tle.mycontent.web.selection;

import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionsController;
import com.tle.web.selection.SelectableInterface;
import com.tle.web.selection.SelectionSession;

/**
 * @author aholland
 */
@Bind
@Singleton
@SuppressWarnings("nls")
public class MyContentSelectable implements SelectableInterface
{
	private static final String SELECTABLE_ID = "mycontent";

	@Inject
	private SectionsController controller;

	@Override
	public SectionInfo createSectionInfo(SectionInfo info, SelectionSession session)
	{
		// Should there ever be one, it should be the only one available
		session.setAllowedSelectNavActions(Collections.singleton(SELECTABLE_ID));
		SectionInfo newInfo = getSearchTree(info);
		return newInfo; // NOSONAR (keeping local variable for readability)
	}

	protected SectionInfo getSearchTree(SectionInfo info)
	{
		return controller.createForward(info, "/access/mycontentselect.do");
	}
}
