package com.tle.web.htmleditor.tinymce.addon.tle.selection;

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
public class FileUploadSelectable implements SelectableInterface
{
	@Inject
	private SectionsController controller;

	@Override
	public SectionInfo createSectionInfo(SectionInfo info, SelectionSession session)
	{
		return getFileUploadTree(info);
	}

	protected SectionInfo getFileUploadTree(SectionInfo info)
	{
		return controller.createForward(info, "/access/tlemceaddon.do"); //$NON-NLS-1$
	}
}
