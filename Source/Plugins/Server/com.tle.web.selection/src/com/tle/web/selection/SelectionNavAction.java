package com.tle.web.selection;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.Label;

public interface SelectionNavAction extends SelectableInterface
{
	Label getLabelForNavAction(SectionInfo info);

	SectionInfo createForwardForNavAction(SectionInfo fromInfo, SelectionSession session);

	boolean isActionAvailable(SectionInfo info, SelectionSession session);

	/**
	 * Should be same as in plugin xml
	 * 
	 * @return
	 */
	String getActionType();

	boolean isShowBreadcrumbs();
}
