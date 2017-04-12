package com.tle.web.viewitem.treeviewer;

import com.tle.web.sections.SectionInfo;
import com.tle.web.viewurl.ItemUrlExtender;

public class DownloadIMSPackageLink implements ItemUrlExtender
{
	private static final long serialVersionUID = 1L;

	@Override
	public void execute(SectionInfo info)
	{
		TreeNavigationSection navSection = info.lookupSection(TreeNavigationSection.class);
		navSection.getModel(info).setDownload(true);
	}
}